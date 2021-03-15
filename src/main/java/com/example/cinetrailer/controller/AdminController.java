package com.example.cinetrailer.controller;

import com.example.cinetrailer.model.Genero;
import com.example.cinetrailer.model.Pelicula;
import com.example.cinetrailer.repo.GeneroRepository;
import com.example.cinetrailer.repo.PeliculaRepository;
import com.example.cinetrailer.service.FileSystemStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private PeliculaRepository peliculaRepository;

    @Autowired
    private GeneroRepository generoRepository;

    @Autowired
    private FileSystemStorageService fileSystemStorageService;

    @GetMapping("")
    ModelAndView index(@PageableDefault(sort = "titulo", size = 5) Pageable pageable) {
        Page<Pelicula> peliculas = peliculaRepository.findAll(pageable);

        return new ModelAndView("admin/index")
                .addObject("peliculas", peliculas);
    }

    @GetMapping("/peliculas/nuevo")
    ModelAndView nuevaPelicula() {
        List<Genero> generos = generoRepository.findAll(Sort.by("titulo"));

        return new ModelAndView("admin/nueva-pelicula")
                .addObject("pelicula", new Pelicula())
                .addObject("generos", generos);
    }

    @PostMapping("/peliculas/nuevo")
    ModelAndView crearPelicula(@Validated Pelicula pelicula, BindingResult bindingResult) {
        if (bindingResult.hasErrors() || pelicula.getPortada().isEmpty()) {
            if (pelicula.getPortada().isEmpty()) {
                bindingResult.rejectValue("portada", "MultipartNotEmpty");
            }

            List<Genero> generos = generoRepository.findAll(Sort.by("titulo"));

            return new ModelAndView("admin/nueva-pelicula")
                    .addObject("pelicula", pelicula)
                    .addObject("generos", generos);
        }
        String rutaPortada = fileSystemStorageService.storage(pelicula.getPortada());
        pelicula.setRutaPortada(rutaPortada);

        peliculaRepository.save(pelicula);
        return new ModelAndView("redirect:/admin");
    }

    @GetMapping("/peliculas/{id}/editar")
    ModelAndView editarPelicula(@PathVariable Integer id) {
        Pelicula pelicula = peliculaRepository.getOne(id);
        List<Genero> generos = generoRepository.findAll(Sort.by("titulo"));

        return new ModelAndView("admin/editar-pelicula")
                .addObject("pelicula", pelicula)
                .addObject("generos", generos);
    }

    @PostMapping("/peliculas/{id}/editar")
    ModelAndView actualizarPelicula(@PathVariable Integer id,
                                    @Validated Pelicula pelicula, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<Genero> generos = generoRepository.findAll(Sort.by("titulo"));
            return new ModelAndView("admin/editar-pelicula")
                    .addObject("pelicula", pelicula)
                    .addObject("generos", generos);
        }
        Pelicula peliculaFromDb = peliculaRepository.getOne(id);
        peliculaFromDb.setTitulo(pelicula.getTitulo());
        peliculaFromDb.setSinopsis(pelicula.getSinopsis());
        peliculaFromDb.setFechaEstreno(pelicula.getFechaEstreno());
        peliculaFromDb.setYoutubeTrailerId(pelicula.getYoutubeTrailerId());
        peliculaFromDb.setGeneros(pelicula.getGeneros());

        if (!pelicula.getPortada().isEmpty()) {
            fileSystemStorageService.delete(peliculaFromDb.getRutaPortada());
            String rutaPortada = fileSystemStorageService.storage(pelicula.getPortada());
            peliculaFromDb.setRutaPortada(rutaPortada);
        }
        peliculaRepository.save(peliculaFromDb);

        return new ModelAndView("redirect:/admin");
    }

    @PostMapping("/peliculas/{id}/eliminar")
    String eliminarPelicula(@PathVariable Integer id) {
        Pelicula pelicula = peliculaRepository.getOne(id);
        peliculaRepository.delete(pelicula);
        fileSystemStorageService.delete(pelicula.getRutaPortada());
        return "redirect:/admin";
    }

}
