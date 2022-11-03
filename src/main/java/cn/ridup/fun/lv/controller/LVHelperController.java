package cn.ridup.fun.lv.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import cn.ridup.base.mix.push.service.ArticleDto;
import cn.ridup.base.mix.push.service.Pusher;
import cn.ridup.fun.lv.service.LVHelperService;
import cn.ridup.fun.lv.service.dto.YuqueHooksDto;
import lombok.extern.slf4j.Slf4j;

/**
 * LV Helper
 *
 * @author ridup.cn
 * @version V0.1
 * @since 2022/3/20 16:38
 */
@RestController
@RequestMapping(value = "/lv")
@Slf4j
public class LVHelperController {

    @Autowired
    private LVHelperService LVHelperService;
    @Resource
    private RestTemplate httpsRestTemplate;
    @Autowired
    private Pusher pusher;

    private String STOCK_URL = "https://api-cn.louisvuitton.cn/api/zhs-cn/catalog/availability/";

    private String PRODUCT_INFO_URL = "https://api-www.louisvuitton.cn/api/zhs-cn/catalog/product/";

    @GetMapping("/send-test")
    public void sendTest() {
        ArticleDto articleDto = new ArticleDto();
        articleDto.setTitle("标题");
        articleDto.setAuthor("作者");
        articleDto.setContent_source_url("https://ridup.cn");
        articleDto.setContent("内容 <b>内容</b>");
        articleDto.setDigest("摘要");

        pusher.send(articleDto);
        System.out.println("==============================");
    }

    public void sendLV(String content, String end) {
        ArticleDto articleDto = new ArticleDto();
        articleDto.setTitle("LV包有货啦！！！");
        articleDto.setAuthor("玉树临风张先生");
        articleDto.setContent_source_url("https://ridup.cn");
        articleDto.setContent(content + end);
        articleDto.setDigest(content);
        pusher.send(articleDto);
    }

    @GetMapping("/start/{identifier}/{skuId}")
    public void start(@PathVariable String identifier, @PathVariable String skuId) throws InterruptedException {

        log.info("lv stock url={} skuId={}", identifier, skuId);
        boolean isInStock = false;
        int count = 5;

        Map productMap = fetchForEntityWithHeader(PRODUCT_INFO_URL + identifier, HttpMethod.GET, null,
            new ParameterizedTypeReference<Map>() {
            });

        String origin = "https://www.louisvuitton.cn";
        String name = (String) productMap.get("name");
        String url = origin + (String) productMap.get("url");

        while (count > 0) {
            Map map = fetchForEntityWithHeader(STOCK_URL + identifier, HttpMethod.GET, null,
                new ParameterizedTypeReference<Map>() {
                });

            List<Map<String, Object>> skuAvailabilityList = (List) map.get("skuAvailability");
            if (CollectionUtils.isEmpty(skuAvailabilityList)) {
                log.error("不存在 skuAvailabilityMap");
            }
            isInStock = skuAvailabilityList.stream()
                .anyMatch(o -> {
                    if ((Boolean) o.get("inStock")) {
                        if (StringUtils.isNotBlank(skuId)) {
                            if (skuId.equals(o.get("skuId")))
                                return true;
                        } else {
                            return true;
                        }
                    }
                    return false;

                });

            if (isInStock) {
                count--;
                String content = "您关注的包包：有货了！有货了！有货了！\n\n";
                String end = "<b>点击进入购买页面：" + name + "\n</b><a href='" + url + "'>" + url + "</a>";
                sendLV(content, end);
            }
        }

    }

    public <E, T> T fetchForEntityWithHeader(String url, HttpMethod method, E request,
        ParameterizedTypeReference<T> type) throws InterruptedException {
        HttpEntity<E> httpEntity = new HttpEntity<>(request, getDefaultHttpHeader(true));
        ResponseEntity<T> haloCommonDtoResponseEntity = httpsRestTemplate.exchange(url, method, httpEntity, type);
        // if (null != haloCommonDtoResponseEntity.getBody() && haloCommonDtoResponseEntity.getBody()
        //     .getStatus()
        //     .equals(org.springframework.http.HttpStatus.UNAUTHORIZED.value())) {
        //     login(yuqueProperties.getHalo()
        //         .getUsername(), yuqueProperties.getHalo()
        //         .getPassword());
        // }
        if (!String.valueOf(HttpStatus.OK.value())
            .equals(haloCommonDtoResponseEntity.getStatusCode())) {
            log.error("lv error =  {} , body = {} ", haloCommonDtoResponseEntity.getStatusCodeValue(),
                haloCommonDtoResponseEntity.getBody());
            Thread.sleep(1000);
        }
        return haloCommonDtoResponseEntity.getBody();
    }

    /**
     * 获取默认的请求头
     *
     * @param tokenCheck 是否需要检查token
     * @return HttpHeaders 请求头
     */
    public HttpHeaders getDefaultHttpHeader(boolean tokenCheck) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.ORIGIN, "https://www.louisvuitton.cn");
        headers.add(HttpHeaders.USER_AGENT,
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36");
        return headers;
    }

}
