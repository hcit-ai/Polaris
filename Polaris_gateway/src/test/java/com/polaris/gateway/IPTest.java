package com.polaris.gateway;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author:Winning
 *
 * Description:
 *
 */
public class IPTest {
    @Test
    public void ipv4Test1() {
        Pattern pattern = Pattern.compile("^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$");
        String[] ips1 = new String[]{
                "0.0.0.0",
                "255.255.255.255",
                "255.255.255.255",
                "0.0.0.1",
                "5.025.25.25",
                "01.1.1.1"
        };
        String[] ips2 = new String[]{
                "111.111.111.256",
                "444.125.123.1",
                "5e:0:0:0:0:0:5668:eeee",
                "::1:2:2:2",
                "11:11:e:1EEE:11:11:200.200.200.200",
                "e:ee:5:e::0.0.0.254",
                "::EfE:120.0.0.1",
                "::120.0.0.1",
                "ee:ee::11.11.11.125"
        };
        for (String ip : ips1) {
            Matcher matcher = pattern.matcher(ip);
            Assert.assertEquals(true, matcher.find());
        }
        for (String ip : ips2) {
            Matcher matcher = pattern.matcher(ip);
            Assert.assertEquals(false, matcher.find());
        }
    }


    @Test
    public void ipv4Test2() {
        Pattern pattern1 = Pattern.compile("^(?:/)(((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?))(?::\\d{1,5}$)");
        Pattern pattern2 = Pattern.compile("^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$");
        String[] ips1 = new String[]{
                "/0.0.0.0:20080",
                "/255.255.255.255:20080",
                "/255.255.255.255:20080",
                "/0.0.0.1:20080",
                "/5.025.25.25:20080",
                "/01.1.1.1:20080"
        };
        for (String ip : ips1) {
            Matcher matcher1 = pattern1.matcher(ip);
            Assert.assertEquals(true, matcher1.matches());
            Matcher matcher2 = pattern2.matcher(matcher1.group(1));
            Assert.assertEquals(true, matcher2.matches());
        }
    }


    @Test
    public void ipv6Test1() {
        Pattern pattern = Pattern.compile("^\\s*((([0-9A-Fa-f]{1,4}:){7}(([0-9A-Fa-f]{1,4})|:))|(([0-9A-Fa-f]{1,4}:){6}(:|((25[0-5]|2[0-4]\\d|[01]?\\d{1,2})(\\.(25[0-5]|2[0-4]\\d|[01]?\\d{1,2})){3})|(:[0-9A-Fa-f]{1,4})))|(([0-9A-Fa-f]{1,4}:){5}((:((25[0-5]|2[0-4]\\d|[01]?\\d{1,2})(\\.(25[0-5]|2[0-4]\\d|[01]?\\d{1,2})){3})?)|((:[0-9A-Fa-f]{1,4}){1,2})))|(([0-9A-Fa-f]{1,4}:){4}(:[0-9A-Fa-f]{1,4}){0,1}((:((25[0-5]|2[0-4]\\d|[01]?\\d{1,2})(\\.(25[0-5]|2[0-4]\\d|[01]?\\d{1,2})){3})?)|((:[0-9A-Fa-f]{1,4}){1,2})))|(([0-9A-Fa-f]{1,4}:){3}(:[0-9A-Fa-f]{1,4}){0,2}((:((25[0-5]|2[0-4]\\d|[01]?\\d{1,2})(\\.(25[0-5]|2[0-4]\\d|[01]?\\d{1,2})){3})?)|((:[0-9A-Fa-f]{1,4}){1,2})))|(([0-9A-Fa-f]{1,4}:){2}(:[0-9A-Fa-f]{1,4}){0,3}((:((25[0-5]|2[0-4]\\d|[01]?\\d{1,2})(\\.(25[0-5]|2[0-4]\\d|[01]?\\d{1,2})){3})?)|((:[0-9A-Fa-f]{1,4}){1,2})))|(([0-9A-Fa-f]{1,4}:)(:[0-9A-Fa-f]{1,4}){0,4}((:((25[0-5]|2[0-4]\\d|[01]?\\d{1,2})(\\.(25[0-5]|2[0-4]\\d|[01]?\\d{1,2})){3})?)|((:[0-9A-Fa-f]{1,4}){1,2})))|(:(:[0-9A-Fa-f]{1,4}){0,5}((:((25[0-5]|2[0-4]\\d|[01]?\\d{1,2})(\\.(25[0-5]|2[0-4]\\d|[01]?\\d{1,2})){3})?)|((:[0-9A-Fa-f]{1,4}){1,2})))|(((25[0-5]|2[0-4]\\d|[01]?\\d{1,2})(\\.(25[0-5]|2[0-4]\\d|[01]?\\d{1,2})){3})))(%.+)?\\s*$");
        String[] ips1 = new String[]{
                "2001:0DB8:02de:0000:0000:0000:0000:0e13",
                "2001:DB8:2de:0000:0000:0000:0000:e13",
                "2001:DB8:2de:000:000:000:000:e13",
                "2001:DB8:2de:00:00:00:00:e13",
                "2001:DB8:2de:0:0:0:0:e13",
                "5e:0:0:0:0:0:5668:eeee",
                "5e:0:0:023:0:0:5668:eeee",
                "5e::5668:eeee",
                "5e::5668:eeee",
                "1::",
                "::1:2:2:2",
                "::",
                //IPv4一致地址,目前已经被取消
                "11:11:e:1EEE:11:11:200.200.200.200",
                "e:ee:5:e::0.0.0.254",
                "::EfE:120.0.0.1",
                "::120.0.0.1",
                "ee:ee::11.11.11.125",
                //环回地址
                "::1",
                "fe80::9",
                //唯一区域位域
                "fc00::7"
        };
        String[] ips2 = new String[]{
                //::出现两次
                "5e::5668::eeee",
                //55555长5位
                "55555:5e:0:0:0:0:0:5668:eeee"
        };
        for (String ip : ips1) {
            Matcher matcher = pattern.matcher(ip);
            Assert.assertEquals(true, matcher.find());
        }
        for (String ip : ips2) {
            Matcher matcher = pattern.matcher(ip);
            Assert.assertEquals(false, matcher.find());
        }
    }

    @Test
    public void ipv6Test2() {
        Pattern pattern1 = Pattern.compile("^(?:/)(\\s*((([0-9A-Fa-f]{1,4}:){7}(([0-9A-Fa-f]{1,4})|:))|(([0-9A-Fa-f]{1,4}:){6}(:|((25[0-5]|2[0-4]\\d|[01]?\\d{1,2})(\\.(25[0-5]|2[0-4]\\d|[01]?\\d{1,2})){3})|(:[0-9A-Fa-f]{1,4})))|(([0-9A-Fa-f]{1,4}:){5}((:((25[0-5]|2[0-4]\\d|[01]?\\d{1,2})(\\.(25[0-5]|2[0-4]\\d|[01]?\\d{1,2})){3})?)|((:[0-9A-Fa-f]{1,4}){1,2})))|(([0-9A-Fa-f]{1,4}:){4}(:[0-9A-Fa-f]{1,4}){0,1}((:((25[0-5]|2[0-4]\\d|[01]?\\d{1,2})(\\.(25[0-5]|2[0-4]\\d|[01]?\\d{1,2})){3})?)|((:[0-9A-Fa-f]{1,4}){1,2})))|(([0-9A-Fa-f]{1,4}:){3}(:[0-9A-Fa-f]{1,4}){0,2}((:((25[0-5]|2[0-4]\\d|[01]?\\d{1,2})(\\.(25[0-5]|2[0-4]\\d|[01]?\\d{1,2})){3})?)|((:[0-9A-Fa-f]{1,4}){1,2})))|(([0-9A-Fa-f]{1,4}:){2}(:[0-9A-Fa-f]{1,4}){0,3}((:((25[0-5]|2[0-4]\\d|[01]?\\d{1,2})(\\.(25[0-5]|2[0-4]\\d|[01]?\\d{1,2})){3})?)|((:[0-9A-Fa-f]{1,4}){1,2})))|(([0-9A-Fa-f]{1,4}:)(:[0-9A-Fa-f]{1,4}){0,4}((:((25[0-5]|2[0-4]\\d|[01]?\\d{1,2})(\\.(25[0-5]|2[0-4]\\d|[01]?\\d{1,2})){3})?)|((:[0-9A-Fa-f]{1,4}){1,2})))|(:(:[0-9A-Fa-f]{1,4}){0,5}((:((25[0-5]|2[0-4]\\d|[01]?\\d{1,2})(\\.(25[0-5]|2[0-4]\\d|[01]?\\d{1,2})){3})?)|((:[0-9A-Fa-f]{1,4}){1,2})))|(((25[0-5]|2[0-4]\\d|[01]?\\d{1,2})(\\.(25[0-5]|2[0-4]\\d|[01]?\\d{1,2})){3})))(%.+)?\\s*)(?::\\d{1,5}$)");
        String[] ips1 = new String[]{
                "/2001:0DB8:02de:0000:0000:0000:0000:0e13:20080",
                "/2001:DB8:2de:0000:0000:0000:0000:e13:20080",
                "/2001:DB8:2de:000:000:000:000:e13:20080",
                "/2001:DB8:2de:00:00:00:00:e13:20080",
                "/2001:DB8:2de:0:0:0:0:e13:20080",
                "/5e:0:0:0:0:0:5668:eeee:20080",
                "/5e:0:0:023:0:0:5668:eeee:20080",
                "/5e::5668:eeee:20080",
                "/5e::5668:eeee:20080",
                "/1:::20080",
                "/::1:2:2:2:20080",
                "/:::20080",
                //IPv4一致地址,目前已经被取消
                "/11:11:e:1EEE:11:11:200.200.200.200:20080",
                "/e:ee:5:e::0.0.0.254:20080",
                "/::EfE:120.0.0.1:20080",
                "/::120.0.0.1:20080",
                "/ee:ee::11.11.11.125:20080",
                //环回地址
                "/::1:20080",
                "/fe80::9:20080",
                //唯一区域位域
                "/fc00::7:20080",
                "/0:0:0:0:0:0:0:1:51421"
        };
        String[] ips2 = new String[]{
                "2001:0DB8:02de:0000:0000:0000:0000:0e13",
                "2001:DB8:2de:0000:0000:0000:0000:e13",
                "2001:DB8:2de:000:000:000:000:e13",
                "2001:DB8:2de:00:00:00:00:e13",
                "2001:DB8:2de:0:0:0:0:e13",
                "5e:0:0:0:0:0:5668:eeee",
                "5e:0:0:023:0:0:5668:eeee",
                "5e::5668:eeee",
                "5e::5668:eeee",
                "1::",
                "::1:2:2:2",
                "::",
                //IPv4一致地址,目前已经被取消
                "11:11:e:1EEE:11:11:200.200.200.200",
                "e:ee:5:e::0.0.0.254",
                "::EfE:120.0.0.1",
                "::120.0.0.1",
                "ee:ee::11.11.11.125",
                //环回地址
                "::1",
                "fe80::9",
                //唯一区域位域
                "fc00::7",
                "0:0:0:0:0:0:0:1"
        };
        for (int i = 0; i < ips1.length; i++) {
            Matcher matcher = pattern1.matcher(ips1[i]);
            matcher.find();
            Assert.assertEquals(ips2[i], matcher.group(1));
        }
    }
}
