/*
MIT License
Copyright(c) 2022 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.futurewei.common.model;

import com.hazelcast.nio.serialization.IdentifiedDataSerializable;





public class ArionDataSerializableFactory implements com.hazelcast.nio.serialization.DataSerializableFactory {

        public static final int FACTORY_ID = 1;

        public static final int ROUTING_RULE_TYPE = 1;

        public static final int GATEWAY_CLUSTER_TYPE = 2;

        public static final int ARION_NODE_TYPE = 3;

        public static final int VPC_TYPE = 4;

        public static final int SECURITY_GROUP_PORT_BINDING = 5;

        public static final int SECURITY_GROUP_RULE = 6;

        @Override
        public IdentifiedDataSerializable create(int typeId) {
            if ( typeId == ROUTING_RULE_TYPE ) {
                return new NeighborRule();
            } else if (typeId == GATEWAY_CLUSTER_TYPE) {
                return new ArionGatewayCluster();
            } else if (typeId == ARION_NODE_TYPE) {
                return new ArionNode();
            } else if (typeId == VPC_TYPE) {
                return new VPC();
            } else if (typeId == SECURITY_GROUP_PORT_BINDING) {
                return new SecurityGroupPortBinding();
            } else if (typeId == SECURITY_GROUP_RULE) {
                return new SecurityGroupRule();
            }else {
                return null;
            }
        }
}



