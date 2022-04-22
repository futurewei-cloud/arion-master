/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package com.futurewei.arionmaster.model;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.springframework.data.annotation.Id;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class RoutingRule implements Serializable {


    private static final long serialVersionUID = 6529685098267757690L;

    @Id
    private String id;
    @EqualsAndHashCode.Exclude
    private String mac;
    @EqualsAndHashCode.Exclude
    private String hostip;
    @EqualsAndHashCode.Exclude
    private String ip;
    @EqualsAndHashCode.Exclude
    private int vni;
    @EqualsAndHashCode.Exclude
    private long version;

    public RoutingRule(String id, String mac, String hostip, String ip, int vni, long version) {
        this.id = id;
        this.mac = mac;
        this.hostip = hostip;
        this.ip = ip;
        this.vni = vni;
        this.version = version;
    }

}
