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

/**
* WebConfig es una clase de configuración que personaliza la configuración de Spring MVC para la aplicación.
* 
* Responsabilidades principales:
* 
*   Define un bean {@link LocaleResolver} que utiliza cookies para gestionar la configuración regional predeterminada de la aplicación 
*   Define un bean {@link LocaleChangeInterceptor} que permite cambiar la configuración regional mediante un parámetro de URL (p. ej., ?language=en
*   Registra el interceptor de cambio de configuración regional para habilitar el cambio de idioma dinámico.
*   Configura los controladores de recursos para servir archivos estáticos desde el directorio local uploads/ a través de la ruta URL /uploads/**.
* 
* Esta configuración habilita la compatibilidad con la internacionalización (i18n) y la gestión de recursos estáticos en la aplicación. 
*/
@Configuration  
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver clr = new CookieLocaleResolver();
        clr.setDefaultLocale(Locale.forLanguageTag("es")); 
        return clr;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("language"); 
        return lci;
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**") // URL pública
                .addResourceLocations("file:uploads/"); // Ruta local del sistema de archivos
    }
}
