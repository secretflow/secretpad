/*
 * Copyright 2024 Ant Group Co., Ltd.
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

package org.secretflow.secretpad.kuscia.v1alpha1.listener;


import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author yutu
 * @date 2024/06/13
 */
@Slf4j
public class ManagedChannelStateListener {

    private final String domainId;

    public ManagedChannelStateListener(ManagedChannel managedChannel, String domainId,
                                       AtomicReference<ConnectivityState> state) {
        this.domainId = domainId;
        listenToChannelStateChanges(managedChannel, ConnectivityState.IDLE, state);
    }


    private void listenToChannelStateChanges(ManagedChannel channel, ConnectivityState lastObservedState,
                                             AtomicReference<ConnectivityState> state) {
        ConnectivityState currentState = channel.getState(true);

        // Handle state change event
        if (currentState != lastObservedState) {
            // Log or perform an action based on the state change
            state.set(currentState);
            log.info("[kuscia] {} Channel state changed from {} to {}", domainId, lastObservedState, currentState);
        }

        // Register a new listener for the next state change
        channel.notifyWhenStateChanged(currentState, () -> listenToChannelStateChanges(channel, currentState, state));
    }

}
