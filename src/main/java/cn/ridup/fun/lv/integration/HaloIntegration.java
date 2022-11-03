package cn.ridup.fun.lv.integration;

import org.springframework.web.bind.annotation.PathVariable;

import cn.ridup.fun.lv.integration.request.HaloPostRequestDto;
import cn.ridup.fun.lv.integration.request.PostStatus;
import cn.ridup.fun.lv.integration.response.login.HaloLoginResponseDto;
import cn.ridup.fun.lv.integration.response.post.BasePostSimpleDTO;
import cn.ridup.fun.lv.integration.response.post.HaloPostResponseDto;
import cn.ridup.fun.lv.integration.support.Page;

/**
 * halo's remote invoke interface
 *
 * @author ridup.cn
 * @version V0.1
 * @since 2022/4/7 15:01
 */
public interface HaloIntegration {

    HaloLoginResponseDto login(String username, String password);

    HaloPostResponseDto createPost(HaloPostRequestDto requestDto);

    HaloPostResponseDto updatePost(HaloPostRequestDto requestDto,Integer postId);

    Page<BasePostSimpleDTO> queryPostList(String title, int page, int size);

    BasePostSimpleDTO updateStatusBy(Integer postId,PostStatus status);
}
