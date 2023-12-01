package hc.uniapp.customize;


import hc.uniapp.album.pojos.Album;
import hc.uniapp.image.pojos.Image;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class SearchDto {
    List<Album> albumList;
    List<Image> imageList;
}
