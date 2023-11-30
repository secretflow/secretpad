/*
 * Copyright 2023 Ant Group Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.secretflow.secretpad.service.util;

import org.secretflow.secretpad.common.util.Base64Utils;
import org.secretflow.secretpad.common.util.EncryptUtils;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.UUIDUtils;
import org.secretflow.secretpad.service.enums.VoteActionEnum;
import org.secretflow.secretpad.service.enums.VoteTypeEnum;
import org.secretflow.secretpad.service.model.approval.VoteReplyBody;
import org.secretflow.secretpad.service.model.approval.VoteReplyMessage;
import org.secretflow.secretpad.service.model.approval.VoteRequestBody;
import org.secretflow.secretpad.service.model.approval.VoteRequestMessage;

import com.google.common.collect.Lists;
import lombok.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * this is an example of TeeCodeGenerateUtil.
 *
 * @author cml
 * @date 2023/10/12
 */
public class TeeCodeGenerateUtil {

    //privateKey
    public static String aliceKey = "==";
    //cert_chain
    public static List<String> aliceCertChain = Lists.newArrayList("", "=");


    public static String bobKey = "";
    public static List<String> bobCertChain = Lists.newArrayList("=", "=");


    public static String generate(String teeResourceID) throws IOException {
        String voteID = UUIDUtils.newUUID();
        VoteRequestBody voteRequestBody = VoteRequestBody.builder()
                .rejectedAction("NODE")
                .approvedAction("TEE_DOWNLOAD," + teeResourceID)
                .type(VoteTypeEnum.TEE_DOWNLOAD.name())
                .approvedThreshold(1)
                .initiator("alice")
                .voteRequestID(voteID)
                .voteCounter("master")
                .voters(Lists.newArrayList("bob"))
                .executors(Lists.newArrayList("alice"))
                .build();
        String body = Base64Utils.encode(JsonUtils.toJSONString(voteRequestBody).getBytes());
        String voteRequestSignature = EncryptUtils.signSHA256withRSA(body.getBytes(), aliceKey);
        List<String> chain = aliceCertChain.stream().map(e -> {
            try {
                return new String(Base64Utils.decode(e));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }).collect(Collectors.toList());
        VoteRequestMessage voteRequestMessage = VoteRequestMessage.builder()
                .body(body)
                .certChain(chain)
                .voteRequestSignature(voteRequestSignature)
                .build();


        //bob

        List<String> bobChain = bobCertChain.stream().map(e -> {
            try {
                return new String(Base64Utils.decode(e));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }).collect(Collectors.toList());
        VoteReplyBody bobVoteReplyBodyBaseInfo = VoteReplyBody.builder()
                .voteRequestID(voteID)
                .voter("bob")
                .action(VoteActionEnum.APPROVE.name())
                .build();
        String bobEncodeMsg = Base64Utils.encode(JsonUtils.toJSONString(bobVoteReplyBodyBaseInfo).getBytes());
        String bobSignature = EncryptUtils.signSHA256withRSA((bobEncodeMsg + voteRequestSignature).getBytes(), bobKey);
        VoteReplyMessage bobVoteReplyMessage = VoteReplyMessage.builder()
                .body(bobEncodeMsg)
                .certChain(bobChain)
                .signature(bobSignature)
                .build();

        VoteResult voteResult = VoteResult.builder()
                .vote_invite(Lists.newArrayList(bobVoteReplyMessage))
                .vote_request(voteRequestMessage)
                .build();

        return JsonUtils.toJSONString(voteResult);
    }


    public static boolean verify(String result) throws Exception {
        VoteResult voteResult = JsonUtils.toJavaObject(result, VoteResult.class);
        VoteRequestMessage voteRequest = voteResult.getVote_request();

        String body = voteRequest.getBody();
        String voteRequestSignature = voteRequest.getVoteRequestSignature();
        List<String> certChain = voteRequest.getCertChain();
        return EncryptUtils.verifySHA256withRSA(body.getBytes(), Base64Utils.encode(certChain.get(0).getBytes()), voteRequestSignature);
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VoteResult {
        private List<VoteReplyMessage> vote_invite;

        private VoteRequestMessage vote_request;
    }
}
