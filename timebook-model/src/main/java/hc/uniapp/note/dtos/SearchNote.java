package hc.uniapp.note.dtos;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SearchNote {
    private String searchColumn;
    private String content;
}
