package hc;

import hc.service.ElasticsearchService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

import static hc.common.constants.ElasticSearchConstants.SEARCH_SUGGESTION;
import static hc.common.constants.ElasticSearchConstants.TIME_BOOK;

@SpringBootTest
public class elasticsearchTest {
    @Resource
    private ElasticsearchService elasticsearchService;
//    @Test
//    void test(){
//        String p="c";
//        Prefix prefix=new Prefix().setPrefix(p);
//        List<String> suggests = elasticsearchService.querySuggest(TIME_BOOK, SEARCH_SUGGESTION, prefix.getPrefix());
//        for (String str:suggests){
//            System.out.println(str);
//        }
//    }
}
