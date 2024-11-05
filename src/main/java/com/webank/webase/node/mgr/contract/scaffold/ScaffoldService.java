/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.node.mgr.contract.scaffold;

import com.webank.scaffold.config.WebaseConfig.ContractInfo;
import com.webank.scaffold.enums.ProjectType;
import com.webank.scaffold.factory.WebaseProjectFactory;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.cert.CertService;
import com.webank.webase.node.mgr.contract.ContractService;
import com.webank.webase.node.mgr.contract.entity.TbContract;
import com.webank.webase.node.mgr.contract.scaffold.entity.ReqProject;
import com.webank.webase.node.mgr.contract.scaffold.entity.RspFile;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.front.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.tools.NetUtils;
import com.webank.webase.node.mgr.tools.NodeMgrTools;
import com.webank.webase.node.mgr.tools.ZipUtils;
import com.webank.webase.node.mgr.user.UserService;
import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * use webank-blockchain-smart-dev-scaffold
 * to generate demo project of contract
 * @author marsli
 */
@Slf4j
@Service
public class ScaffoldService {
    @Autowired
    private ContractService contractService;
    @Autowired
    private CertService certService;
    @Autowired
    private FrontService frontService;
    @Autowired
    private UserService userService;
    @Autowired
    private FrontInterfaceService frontInterfaceService;

    private static final String GRADLE_WRAPPER_DIR = "gradle";
    private static final String GRADLE_VERSION = "6.6.1";

    private static final String OUTPUT_DIR = "output";
    private static final String SOL_OUTPUT_DIR = "scaffoldSolDir";

    private static final String ZIP_SUFFIX = ".zip";
    private static final String OUTPUT_ZIP_DIR = OUTPUT_DIR + File.separator + "zip";

    /**
     * generate by contract with sol
      */
    public RspFile exportProject(ReqProject reqProject) {
        String artifactName = reqProject.getArtifactName();
        // check dir exist
        File checkProjectDir = new File(OUTPUT_DIR + File.separator + artifactName);
        if (checkProjectDir.exists()) {
            boolean result = NodeMgrTools.deleteDir(checkProjectDir);
            log.warn("exportProject dir exist: {}, now delete it result:{}", artifactName, result);
        }
        // get contract info list
        List<Integer> contractIdList = reqProject.getContractIdList();
        List<TbContract> tbContractList = new ArrayList<>();
        for (Integer id : contractIdList) {
            TbContract contract = contractService.queryByContractId(id);
            if (contract == null) {
                log.error("exportProject contract not exist or not compiled, id:{}", id);
                throw new NodeMgrException(ConstantCode.INVALID_CONTRACT_ID);
            }
            if (StringUtils.isBlank(contract.getContractAbi())) {
                log.error("contract abi is empty, of name:{}, not support lib contract", contract.getContractName());
                throw new NodeMgrException(ConstantCode.CONTRACT_ABI_EMPTY.getCode(), "library contract not support export project, please exclude this contract:" + contract.getContractName());
            }
            tbContractList.add(contract);
        }
        // get from front
        TbFront front = frontService.getById(reqProject.getFrontId());
        if (front == null) {
            log.error("exportProject front not exist:{}", reqProject.getFrontId());
            throw new NodeMgrException(ConstantCode.INVALID_FRONT_ID);
        }
        // get front's p2p ip and channel port
        String peersIpPort = reqProject.getChannelIp() + ":" + reqProject.getChannelPort();

        log.info("exportProject get frontNodeConfig:{}", peersIpPort);
        // get front's sdk key cert
        Map<String, String> sdkMap = certService.getFrontSdkContent(front.getFrontId());
        log.info("exportProject get sdkMap size:{}", sdkMap.size());
        // get user private key if set
        List<String> userAddressList = reqProject.getUserAddressList();
        String hexPrivateKeyListStr = "";
        if (userAddressList != null && !userAddressList.isEmpty()) {
            hexPrivateKeyListStr = this.handleUserList(reqProject.getGroupId(), userAddressList);
            //hexPrivateKeyListStr = userService.queryUserDetail(reqProject.getGroupId(), userAddressList.get(0));
        }
        // generate
        String projectPath = this.generateProject(peersIpPort, reqProject.getGroup(), reqProject.getArtifactName(),
            tbContractList, reqProject.getGroupId(), hexPrivateKeyListStr, sdkMap);
        String zipFileName = artifactName + ZIP_SUFFIX;
        try {
            ZipUtils.generateZipFile(projectPath, OUTPUT_ZIP_DIR, artifactName, zipFileName);
        } catch (Exception e) {
            log.error("exportProject generateZipFile failed:[]", e);
            // if failed, delete project dir
            boolean result = checkProjectDir.delete();
            log.error("zip failed, now delete project dir, result:{}", result);
        }
        String zipFileFullPath = OUTPUT_ZIP_DIR + File.separator + zipFileName;
        log.info("exportProject zipFileName:{}, zipFileFullPath{}", zipFileName, zipFileFullPath);
        RspFile rspFile = new RspFile();
        rspFile.setFileName(zipFileName);
        rspFile.setFileStreamBase64(NodeMgrTools.fileToBase64(zipFileFullPath));
        return rspFile;
    }

    /**
     * generate project
     * @param peers ip channel port list
     * @param projectGroup
     * @param artifactName
     * @param tbContractList
     * @param groupId
     * @param hexPrivateKeyListStr
     * @param sdkMap
     * @return path string of project
     */
    private String generateProject(String peers, String projectGroup, String artifactName,
        List<TbContract> tbContractList, String groupId, String hexPrivateKeyListStr, Map<String, String> sdkMap) {
        log.info("generateProject sdkMap size:{}", sdkMap.size());
        List<ContractInfo> contractInfoList = this.handleContractList(groupId, tbContractList);
        String need = contractInfoList.stream().map(ContractInfo::getContractName)
            .collect(Collectors.joining(","));
        log.info("generateProject need:{}", need);

        WebaseProjectFactory projectFactory = new WebaseProjectFactory(
            projectGroup, artifactName,
            OUTPUT_DIR, GRADLE_VERSION,
            contractInfoList,
            peers,
            groupId, hexPrivateKeyListStr, sdkMap);
        boolean createResult = projectFactory.createProject();
        if (!createResult) {
            log.error("generateProject createProject failed");
            throw new NodeMgrException(ConstantCode.GENERATE_CONTRACT_PROJECT_FAIL);
        }
        log.info("generateProject projectGroup:{},artifactName:{},OUTPUT_DIR:{},frontChannelIpPort:{},groupId:{}",
            projectGroup, artifactName, OUTPUT_DIR, peers, groupId);

        String projectDir = OUTPUT_DIR + File.separator + artifactName;
        log.info("generateProject result:{}", projectDir);
        return projectDir;
    }

    private List<ContractInfo> handleContractList(String groupId, List<TbContract> contractList) {
        log.info("handleContractList contractList:{}", contractList);
        List<ContractInfo> contractInfoList = new ArrayList<>();
        log.info("handleContractList param contractList size:{}", contractList.size());
        for (TbContract contract : contractList) {
            String sourceCodeBase64 = contract.getContractSource();
            String solSourceCode = new String(Base64.getDecoder().decode(sourceCodeBase64));
            String contractName = contract.getContractName();
            String contractAddress = contract.getContractAddress();
            String contractAbi = contract.getContractAbi();
            String bytecodeBin = contract.getBytecodeBin();

            ContractInfo contractInfo = new ContractInfo();
            contractInfo.setSolRawString(solSourceCode);
            contractInfo.setContractName(contractName);
            contractInfo.setContractAddress(contractAddress);
            contractInfo.setAbiStr(contractAbi);
            if (frontInterfaceService.getCryptoType(groupId) == CryptoType.SM_TYPE) {
                contractInfo.setBinStr("");
                contractInfo.setSmBinStr(bytecodeBin);
            } else {
                contractInfo.setBinStr(bytecodeBin);
                contractInfo.setSmBinStr("");
            }
            contractInfoList.add(contractInfo);
        }
        log.info("handleContractList result contractInfoList size:{}", contractInfoList.size());
        log.info("handleContractList contractList:{}", contractInfoList);
        return contractInfoList;
    }

    /**
     * userP12PathList
     * @return: String of list, e.g. 123,123,123
     */
    private String handleUserList(String groupId, List<String> userAddressList) {
        List<String> keyList = new ArrayList<>();
        for (String address : userAddressList) {
            String hexPrivateKey = "";
            if (StringUtils.isNotBlank(address)) {
                hexPrivateKey = userService.queryUserDetail(groupId, address);
            }
            log.info("exportProject get hexPrivateKey length:{}", hexPrivateKey.length());
            keyList.add(hexPrivateKey);
        }
        return StringUtils.join(keyList, ",");
    }

    /**
     * telnet channel port to check reachable
     * @param nodeIp
     * @param channelPort
     * @return
     */
    public Boolean telnetChannelPort(String nodeIp, int channelPort) {
        Pair<Boolean, Integer> telnetResult = NetUtils.checkPorts(nodeIp, 2000, channelPort);
        // if true, telnet success, port is in use, which means node's channelPort is correct
        log.info("telnet {}:{} result:{}", nodeIp, channelPort, telnetResult);
        return telnetResult.getLeft();
    }
}
