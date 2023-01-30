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

package com.futurewei.banchmark;

import com.futurewei.banchmark.service.ArionmasterNeighborRuleTest;
import com.futurewei.banchmark.service.ArionmasterSecurityGroupTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BanchmarkApplication implements CommandLineRunner {


    @Autowired
    private ArionmasterNeighborRuleTest arionmasterNeighborRuleTest;

    @Autowired
    private ArionmasterSecurityGroupTest arionmasterSecurityGroupTest;

    public static void main(String[] args) {
        SpringApplication.run(BanchmarkApplication.class, args);
    }


    @Override
    public void run(String... args) throws InterruptedException {
        if (args.length > 0 && args[0].equals("neighborrule"))
        {
            if (args[1].equals("batchInsert")) {
                arionmasterNeighborRuleTest.bulkInsertNeighborRule();
            } else {
                arionmasterNeighborRuleTest.insertNeighborRule();
            }
        }
        else if (args.length > 0 && args[0].equals("securitygroupportbinding"))
        {
            System.out.println("test");
            arionmasterSecurityGroupTest.insertSecurityGroup();
        } else {

        }

    }

}
