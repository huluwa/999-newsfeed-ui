package oms.sns.service.facebook.model;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import oms.sns.service.facebook.util.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Attachment {
    public String name;
    public String href;
    public String caption;
    public String description;
    public List<AttachmentProperty> properties;
    public AttachmentMedia media;
    public Map<String,String> additionalInfo;
    public JSONObject jsonAttachment;

    /**
     * @return a JSON representation of attachment.
     */
    public JSONObject toJson() {
        jsonAttachment = new JSONObject();

        putJsonObject( "name", name );
        putJsonObject( "href", href );
        putJsonObject( "caption", caption );
        putJsonObject( "description", description );

        putJsonProperties();
        putJsonMedia();
        putJsonAdditionalInfo();

        return jsonAttachment;
    }

    private void putJsonObject( final String key, final Object value ) {
        if ( jsonAttachment == null ) {
            // this should only be called by toJson() after the object is initialized
            return;
        }
        try {
            jsonAttachment.put( key, value );
        }
        catch ( Exception ignored ) {
            // ignore
        }
    }

    private void putJsonProperties() {
        if ( properties == null || properties.isEmpty() ) {
            return;
        }

        JSONObject jsonProperties = new JSONObject();
        for ( AttachmentProperty link : properties ) {
            try {
                if ( !StringUtils.isEmpty( link.caption ) ) {
                    if ( !StringUtils.isEmpty( link.text ) && !StringUtils.isEmpty( link.href ) ) {
                        jsonProperties.put( link.caption, link.toJson() );
                    } else if ( !StringUtils.isEmpty( link.text ) ) {
                        jsonProperties.put( link.caption, link.text );
                    }
                }
            }
            catch ( JSONException exception ) {
               // throw ExtensibleClient.runtimeException( exception );
                
            }
        }

        putJsonObject( "properties", jsonProperties );
    }

    private void putJsonMedia() {
        if ( media == null ) {
            return;
        }

        putJsonObject( "media", media.toJson() );
    }

    private void putJsonAdditionalInfo() {
        if ( additionalInfo == null || additionalInfo.isEmpty() ) {
            return;
        }

        for ( String key : additionalInfo.keySet() ) {
            putJsonObject( key, additionalInfo.get( key ) );
        }
    }

    /**
     * @return a JSON-encoded String representation of this template. The resulting String is appropriate for passing to the Facebook API server.
     */
    public String toJsonString() {
        return this.toJson().toString();
    }

    
    public abstract class AttachmentMedia {
        public String mediaType;

        /**
         * @return JSON Array of this media attachment.
         */
        public abstract JSONArray toJson();

        /**
         * 
         * @return String of JSON Array of this media attachment.
         */
        public abstract String toJsonString();

    }
    
    
    public class AttachmentMediaFlash extends AttachmentMedia{
        public String swfsrc;
        public String imgsrc;
        public Integer width;
        public Integer height;
        public Integer expandedWidth;
        public Integer expandedHeight;
        public JSONObject jsonObject;

        /**
         * @return a JSON representation of attachment.
         */
        @Override
        public JSONArray toJson() {
            jsonObject = new JSONObject();
            putJsonProperty( "type", mediaType );
            putJsonProperty( "swfsrc", swfsrc );
            putJsonProperty( "imgsrc", imgsrc );
            if ( height != null ) {
                putJsonProperty( "height", height );
            }
            if ( width != null ) {
                putJsonProperty( "width", width );
            }
            if ( expandedHeight != null ) {
                putJsonProperty( "expanded_height", expandedHeight );
            }
            if ( expandedWidth != null ) {
                putJsonProperty( "expanded_width", expandedWidth );
            }

            JSONArray jsonArray = new JSONArray();
            jsonArray.put( jsonObject );

            return jsonArray;
        }

        private JSONObject putJsonProperty( final String key, final Object value ) {
            try {
                jsonObject.put( key, value );
            }
            catch ( Exception ignored ) {
                // ignore
            }

            return jsonObject;
        }

        @Override
        public String toJsonString() {
            return this.toJson().toString();
        }
    }
    
    
        
        public class AttachmentMediaImage extends AttachmentMedia{
            public Map<String,String> images;

            /**
             * Constructor.
             */
            public AttachmentMediaImage() {
                this.mediaType = "image";
                images = new TreeMap<String,String>();
            }

            /**
             * Add an image. Max number of images is 5.
             * 
             * @param src
             *            URL of the image.
             * @param href
             *            Location to link image to.
             */
            public void addImage( final String src, final String href ) {
                if ( StringUtils.isEmpty( src ) || StringUtils.isEmpty( href ) ) {
                    return;
                }
                if ( images.size() > 4 ) {
                    return;
                }

                images.put( src, href );
            }

            /**
             * @return a JSON representation of attachment.
             */
            @Override
            public JSONArray toJson() {
                JSONArray jsonArray = new JSONArray();
                for ( String key : images.keySet() ) {
                    JSONObject image = new JSONObject();

                    try {
                        image.put( "type", mediaType );
                    }
                    catch ( Exception ignored ) {
                        // ignore
                    }

                    try {
                        image.put( "src", key );
                    }
                    catch ( Exception ignored ) {
                        // ignore
                    }

                    try {
                        image.put( "href", images.get( key ) );
                    }
                    catch ( Exception ignored ) {
                        // ignore
                    }

                    jsonArray.put( image );
                }

                return jsonArray;
            }

            @Override
            public String toJsonString() {
                return this.toJson().toString();
            }
            
        }  
        
        
        public class AttachmentMediaMP3  extends AttachmentMedia{
            
            public String src;
            public String title;
            public String artist;
            public String album;
            public JSONObject jsonObject;

            /**
             * Construct a MP3 attachment.
             * 
             * @param src
             *            URL of the MP3 file to be rendered within Facebook's MP3 player widget.
             * @param title
             *            MP3 title. (optional)
             * @param artist
             *            MP3 artist. (optional)
             * @param album
             *            MP3 album. (optional)
             */
            public AttachmentMediaMP3( final String src, final String title, final String artist, final String album ) {
                this.mediaType = "mp3";
                this.src = src;
                this.title = title;
                this.artist = artist;
                this.album = album;
            }

            /**
             * Construct a MP3 attachment.
             */
            public AttachmentMediaMP3() {
                this.mediaType = "mp3";
            }

            /**
             * @return a JSON representation of attachment.
             */
            @Override
            public JSONArray toJson() {
                jsonObject = new JSONObject();
                putJsonProperty( "type", mediaType );
                putJsonProperty( "src", src );
                if ( !StringUtils.isEmpty( title ) ) {
                    putJsonProperty( "title", title );
                }
                if ( !StringUtils.isEmpty( artist ) ) {
                    putJsonProperty( "artist", artist );
                }
                if ( !StringUtils.isEmpty( album ) ) {
                    putJsonProperty( "album", album );
                }

                JSONArray jsonArray = new JSONArray();
                jsonArray.put( jsonObject );

                return jsonArray;
            }

            private JSONObject putJsonProperty( final String key, final Object value ) {
                try {
                    jsonObject.put( key, value );
                }
                catch ( Exception ignored ) {
                    // ignore
                }

                return jsonObject;
            }

            @Override
            public String toJsonString() {
                return this.toJson().toString();
            }
        }
        
        
        public class AttachmentMediaVideo extends AttachmentMedia{
            
            public String videoSrc;
            public String previewImg;
            public String title;
            public String type;
            public String link;
            public JSONObject jsonObject;

            /**
             * Construct a video attachment.
             */
            public AttachmentMediaVideo() {
                this.mediaType = "video";
            }

            /**
             * @return a JSON representation of attachment.
             */
            @Override
            public JSONArray toJson() {
                jsonObject = new JSONObject();
                putJsonProperty( "type", mediaType );
                putJsonProperty( "video_src", videoSrc );
                putJsonProperty( "preview_img", previewImg );

                if ( !StringUtils.isEmpty( title ) ) {
                    putJsonProperty( "video_title", title );
                }
                if ( !StringUtils.isEmpty( type ) ) {
                    putJsonProperty( "video_type", type );
                }
                if ( !StringUtils.isEmpty( link ) ) {
                    putJsonProperty( "video_link", link );
                }

                JSONArray jsonArray = new JSONArray();
                jsonArray.put( jsonObject );

                return jsonArray;
            }

            private JSONObject putJsonProperty( final String key, final Object value ) {
                try {
                    jsonObject.put( key, value );
                }
                catch ( Exception ignored ) {
                    // ignore
                }

                return jsonObject;
            }

            @Override
            public String toJsonString() {
                return this.toJson().toString();
            }
        }
     
        
        public class AttachmentProperty {
            public String caption;
            public String href;
            public String text;

            /**
             * @return JSON Object of this attachment link.
             */
            public JSONObject toJson() {
                JSONObject link = new JSONObject();
                try {
                    link.put( "text", text );
                    link.put( "href", href );
                }
                catch ( JSONException ignored ) {
                    //
                }

                return link;
            }

            /**
             * 
             * @return String of JSON Array of this attachment link.
             */
            public String toJsonString() {
                return this.toJson().toString();
            }
        }
    
        
        public class BundleActionLink {
            public String text;
            public String href;

            /**
             * Constructor. If you use this version, you must make sure you set both the 'text' and 'href' fields before trying to submit your template, otherwise it will not
             * serialize correctly.
             */
            public BundleActionLink() {
                // empty
            }

            /**
             * Constructor.
             * 
             * @param text
             *            the text to display for the action.
             * @param href
             *            the action link (may include tokens).
             */
            public BundleActionLink( String text, String href ) {
                this.text = text;
                this.href = href;
            }

            /**
             * @return a JSON representation of this template.
             */
            public JSONObject toJson() {
                JSONObject result = new JSONObject();
                if ( ( text == null ) || ( href == null ) || ( "".equals( text ) ) || ( "".equals( href ) ) ) {
                    return result;
                }


                try {
                    result.put( "text", text );
                    result.put( "href", href );
                }
                catch ( Exception ignored ) {
                    // ignore
                }
                return result;
            }

            /**
             * @return a JSON-encoded String representation of this template. The resulting String is appropriate for passing to the Facebook API server.
             */
            public String toJsonString() {
                return this.toJson().toString();
            }

        }

            
}
