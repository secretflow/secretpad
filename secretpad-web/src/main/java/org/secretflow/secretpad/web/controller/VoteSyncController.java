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

package org.secretflow.secretpad.web.controller;

import org.secretflow.secretpad.service.VoteSyncService;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;
import org.secretflow.secretpad.service.model.datasync.vote.VoteSyncRequest;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * VoteSyncController.
 *
 * @author cml
 * @date 2023/11/02
 */
@RestController
@RequestMapping(value = "/api/v1alpha1/vote_sync")
public class VoteSyncController {

    private final VoteSyncService voteSyncService;

    public VoteSyncController(VoteSyncService voteSyncService) {
        this.voteSyncService = voteSyncService;
    }

    /**
     * the sync method of edge push to center
     *
     * @param voteSyncRequest
     * @return
     */
    @PostMapping(value = "/create", consumes = "application/json")
    public SecretPadResponse<Object> sync(@Valid @RequestBody VoteSyncRequest voteSyncRequest) {
        voteSyncService.sync(voteSyncRequest.getDbSyncRequests());
        return SecretPadResponse.success();
    }
}
