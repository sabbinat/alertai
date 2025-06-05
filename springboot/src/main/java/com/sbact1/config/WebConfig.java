package com.sbact1.config;

import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import org.springframework.lang.NonNull;
import org.springframework.web.servlet.LocaleResolver;

@Configuration  
public class WebConfig implements WebMvcConfigurer {

    // Define el LocaleResolver que determinará el idioma predeterminado de la aplicación
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver clr = new CookieLocaleResolver();
        clr.setDefaultLocale(Locale.forLanguageTag("es")); 
        return clr;
    }

    // Interceptor que permite cambiar el idioma a través de un parámetro en la URL
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("language"); // El idioma se cambiará usando ?language= (por ejemplo ?language=en)
        return lci;
    }

    // Agrega el interceptor de cambio de idioma al registro de interceptores
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    // Configura la ruta de acceso a recursos estáticos (imágenes subidas)
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**") // URL pública
                .addResourceLocations("file:uploads/"); // Ruta local del sistema de archivos
    }
}
