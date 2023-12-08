package hc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import hc.common.customize.Sensitive;
import hc.common.dtos.ResponseResult;

public interface SensitivesService extends IService<Sensitive> {

    ResponseResult checkSensitives(String content);
}
