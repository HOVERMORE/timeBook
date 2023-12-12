package hc.apis.note.fallback;

import hc.apis.note.INoteServiceClient;
import hc.uniapp.note.pojos.Note;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

@Component
public class INoteServiceClientFallback implements INoteServiceClient {
    @Override
    public Note getNote(@PathVariable String id) {
        return null;
    }
}
