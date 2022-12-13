package us.shandian.giga.util;

import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.services.youtube.ItagItem;
import org.schabi.newpipe.extractor.stream.DeliveryMethod;
import org.schabi.newpipe.extractor.stream.Stream;

public class OtherSiteVideo extends Stream {



    /**
     * Instantiates a new {@code Stream} object.
     *
     * @param id             the identifier which uniquely identifies the file, e.g. for YouTube
     *                       this would be the itag
     * @param content        the content or URL, depending on whether isUrl is true
     * @param isUrl          whether content is the URL or the actual content of e.g. a DASH
     *                       manifest
     * @param format         the {@link MediaFormat}, which can be null
     * @param deliveryMethod the delivery method of the stream
     * @param manifestUrl    the URL of the manifest this stream comes from (if applicable,
     */
    public OtherSiteVideo(String id, String content, boolean isUrl, MediaFormat format, DeliveryMethod deliveryMethod, String manifestUrl) {
        super(id, content, isUrl, format, deliveryMethod, manifestUrl);
    }

    @Override
    public ItagItem getItagItem() {
        return null;
    }

//    public String getDirect_source()
//    {
//        return direct_source;
//    }
}
