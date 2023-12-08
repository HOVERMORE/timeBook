package hc.constants;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WxConstants {
    public static String APP_ID;
    public static String APP_SECRET;
    public static String OPEN_ID_URL;

    @Value("${wx.app_id}")
    private void setAppId(String id){APP_ID=id;}
    @Value("${wx.app_secret}")
    private void setAppSecret(String secret){APP_SECRET=secret;}
    @Value("${wx.open_id_url}")
    private void setOpenIdUrl(String url){OPEN_ID_URL=url;}
}
