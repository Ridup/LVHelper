package cn.ridup.fun.lv.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import cn.ridup.fun.lv.config.properties.YuqueProperties;
import cn.ridup.fun.lv.integration.HaloIntegration;
import cn.ridup.fun.lv.integration.request.HaloPostRequestDto;
import cn.ridup.fun.lv.integration.request.PostParam;
import cn.ridup.fun.lv.integration.response.post.BasePostSimpleDTO;
import cn.ridup.fun.lv.integration.support.Page;
import cn.ridup.fun.lv.service.LVHelperService;
import cn.ridup.fun.lv.service.convertor.DocDetailConvertor;
import cn.ridup.fun.lv.service.convertor.HaloDataConvertor;
import cn.ridup.fun.lv.service.dto.DocDetailDto;
import cn.ridup.fun.lv.service.dto.DocDetailSerializer;
import cn.ridup.fun.lv.service.dto.YuqueHooksDto;
import lombok.extern.slf4j.Slf4j;

/**
 * the implements of webhooks
 *
 * @author ridup
 * @version 0.1.0
 * @since 2022/4/5 23:43
 */
@Service
@Slf4j
public class LVHelperServiceImpl implements LVHelperService {

    @Resource
    private HaloIntegration haloIntegration;

    @Resource
    private YuqueProperties yuqueProperties;

    @Override
    public void hooks(YuqueHooksDto yuqueHooksDto) {
        DocDetailDto docDetailDto = DocDetailConvertor.convertToDocDetail(yuqueHooksDto);
        log.info("the doc detail after converted is ==> {}", docDetailDto);
        Assert.notNull(docDetailDto, "yu que doc detail must not be null!");
        DocDetailSerializer data = docDetailDto.getData();
        Assert.notNull(data, "yu que doc detail data must not be null!");
        Page<BasePostSimpleDTO> postList = haloIntegration.queryPostList(data.getTitle(), 0, 20);
        boolean isExist = false;
        Integer postId = null;
        if (postList.getTotal() > 0) {
            isExist = postList.getContent()
                .stream()
                .anyMatch(basePostSimpleDTO -> basePostSimpleDTO.getSlug()
                    .equals(data.getSlug()));
            if(!isExist) {
                postList = haloIntegration.queryPostList(null, 0, 9999);
                isExist = postList.getContent()
                    .stream()
                    .anyMatch(basePostSimpleDTO -> basePostSimpleDTO.getSlug()
                        .equals(data.getSlug()));
            }
            if(isExist){
                BasePostSimpleDTO first = postList.getContent()
                    .stream()
                    .filter(basePostSimpleDTO -> basePostSimpleDTO.getSlug()
                        .equals(data.getSlug()))
                    .findFirst()
                    .orElseThrow();
                postId = first.getId();
            }
        }

        PostParam postParam = HaloDataConvertor.convert(data, yuqueProperties.getHalo());

        switch(data.getWebhookSubjectType()){
            case NEW_REVIEW:
            case COMPLETE_REVIEW:
            case CANCEL_REVIEW:
                haloIntegration.updateStatusBy(postId, postParam.getStatus());
                break;
            case PUBLISH:
            case UPDATE:
            default:
                HaloPostRequestDto requestDto = new HaloPostRequestDto();
                requestDto.setPostParam(postParam);
                if (isExist) {
                    haloIntegration.updatePost(requestDto,postId );
                } else {
                    haloIntegration.createPost(requestDto);
                }
        }
    }

}
