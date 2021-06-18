package com.gratig.proxy.littleproxy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gratig.proxy.entity.Category;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.littleshoot.proxy.impl.ProxyUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Component
@Slf4j
public class ProxyManager {
    private final ObjectMapper mapper = new ObjectMapper();
    private boolean enabled=true;
    private LocalTime startBlock = LocalTime.of(15,40);
//    private LocalTime endBlock = LocalTime.of(8,0);

    public List<Category> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<Category> blocks) {
        this.blocks = blocks;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private List<Category> blocks = Collections.synchronizedList(new ArrayList<>());
    private HttpProxyServer server;

    public ProxyManager(@Value("${server.proxy.port}") int port, @Value("${category.file.loc}") String fileLoc) throws IOException {
        load(fileLoc);
        this.server =
                DefaultHttpProxyServer.bootstrap()
                        .withPort(port)
                        .withFiltersSource(new HttpFiltersSourceAdapter() {
                            public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                                return new HttpFiltersAdapter(originalRequest) {
                                    @Override
                                    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
                                        if(LocalTime.now().isBefore(startBlock)) {
                                            return null;
                                        }

                                        if (httpObject instanceof HttpRequest && enabled) {
                                            HttpRequest request = (HttpRequest) httpObject;
                                            String uri = request.uri();
                                            log.info(uri);
                                            if (clean(uri).endsWith(".io") || blocks.stream()
                                                    .filter(Category::isBlocked)
                                                    .map(Category::getSites)
                                                    .flatMap(Set::stream)
                                                    .anyMatch(x -> uri.toLowerCase().contains(x))) {
                                                return getBadGatewayResponse();
                                            } else {
                                                blocks.stream().filter(c -> c.getName().equals("other")).findFirst().ifPresent(o ->
                                                {
                                                    String cleanUri = clean(uri);
                                                    if(cleanUri != null) {
                                                        o.getSites().add(cleanUri);
                                                    }
                                                });
                                            }
                                        }
                                        return null;
                                    }
                                };
                            }
                        })
                        .start();
    }

    private void load(String fileLoc) throws IOException {
        List<Category> categories = mapper.readValue(new File(fileLoc), new TypeReference<List<Category>>() {});
        blocks.clear();
        blocks.addAll(categories);
    }

    private String clean(String uri) {
        return Optional.ofNullable(uri)
                .filter(StringUtils::isNotBlank)
                .map(u -> u.substring(0,u.indexOf(':')))
                .orElse(null);
    }

    private static HttpResponse getBadGatewayResponse() {
        String body = "<!DOCTYPE HTML \"-//IETF//DTD HTML 2.0//EN\">\n"
                + "<html><head>\n"
                + "<title>" + "Bad Gateway" + "</title>\n"
                + "</head><body>\n"
                + "An error occurred"
                + "</body></html>\n";
        byte[] bytes = body.getBytes(Charset.forName("UTF-8"));
        ByteBuf content = Unpooled.copiedBuffer(bytes);
        HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_GATEWAY, content);
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, bytes.length);
        response.headers().set("Content-Type", "text/html; charset=UTF-8");
        response.headers().set("Date", ProxyUtils.formatDate(new Date()));
        response.headers().set(HttpHeaders.Names.CONNECTION, "close");
        return response;
    }

}
