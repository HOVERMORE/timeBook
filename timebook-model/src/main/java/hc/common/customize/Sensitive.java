package hc.common.customize;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("tb_sensitive")
@Accessors(chain = true)
public class Sensitive implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private String sensitiveId;

    private String sensitives;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
