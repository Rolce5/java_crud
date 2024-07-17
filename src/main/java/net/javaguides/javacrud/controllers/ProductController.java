package net.javaguides.javacrud.controllers;

import jakarta.validation.Valid;
import net.javaguides.javacrud.models.ProductDto;
import net.javaguides.javacrud.models.Products;
import net.javaguides.javacrud.services.ProductsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductsRepository repo;

    @GetMapping({ "", "/" })
    public String showProductList(Model model){
        List<Products> products = repo.findAll();

        model.addAttribute("products", products);
        return "products/index";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model){
        ProductDto productDto = new ProductDto();
        model.addAttribute("productDto",productDto);
        return "products/create";
    }

    @PostMapping("/create")
    public String store(@Valid @ModelAttribute ProductDto productDto, BindingResult result){
        if(productDto.getImageFile().isEmpty()){
            result.addError(new FieldError("productDto","imageFile","The image file is required"));
        }

        if(result.hasErrors()){
            return "products/create";
        }
//        save image file
        MultipartFile image = productDto.getImageFile();
        Date createdAt = new Date();
        String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

        try {
            String uploadDir = "public/images/";
            Path uploadPath = Paths.get(uploadDir);

            if(!Files.exists(uploadPath)){
                Files.createDirectories(uploadPath);
            }

            try (InputStream inputStream = image.getInputStream()){
                Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e){
            System.out.println("Exception: " + e.getMessage());
        }

        Products product = new Products();
        product.setName(productDto.getName());
        product.setBrand(productDto.getBrand());
        product.setCategory(productDto.getCategory());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescription());
        product.setCreatedAt(createdAt);
        product.setImageFileName(storageFileName);

        repo.save(product);
        return "redirect:/products";
    }

    @GetMapping("/edit")
    public String edit(Model model, @RequestParam int id){
        try{
            Products product = repo.findById(id).get();
            model.addAttribute("product",product);

            ProductDto productDto = new ProductDto();
            productDto.setName(product.getName());
            productDto.setBrand(product.getBrand());
            productDto.setCategory(product.getCategory());
            productDto.setPrice(productDto.getPrice());
            productDto.setDescription(product.getDescription());

            model.addAttribute("productDto", productDto);
        } catch (Exception e){
            System.out.println("Exception: " + e.getMessage());
            return "redirect:/products";
        }
        return "products/edit";
    }

    @PostMapping("/edit")
    public String update(Model model, @RequestParam int id, @Valid @ModelAttribute ProductDto productDto, BindingResult result){
        try{
            Products product = repo.findById(id).get();
            model.addAttribute("product", product);

            if(result.hasErrors()){
                return "products/edit";
            }

            if(!productDto.getImageFile().isEmpty()){
//                delete old
                String uploadDir = "public/images/";
                Path oldImagePath = Paths.get(uploadDir + product.getImageFileName());

                try {
                    Files.delete(oldImagePath);
                } catch (Exception e){
                    System.out.println("Exception: " + e.getMessage());
                }

//                save new image file
                MultipartFile image = productDto.getImageFile();
                Date createdAt = new Date();
                String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

                try(InputStream inputStream = image.getInputStream()){
                    Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
                            StandardCopyOption.REPLACE_EXISTING);
                }

                product.setImageFileName(storageFileName);
            }

            product.setName(productDto.getName());
            product.setBrand(productDto.getBrand());
            product.setCategory(productDto.getCategory());
            product.setPrice(productDto.getPrice());
            product.setDescription( productDto.getDescription());

            repo.save(product);

        } catch (Exception e){
            System.out.println("Exception: " + e.getMessage());
        }
        return "redirect:/products";
    }

    @GetMapping("/delete")
    public String destroy(@RequestParam int id){
        try{
            Products product = repo.findById(id).get();
//            delete image from the public folder
            Path imagePath = Paths.get("public/images" + product.getImageFileName());
            try {
                Files.delete(imagePath);
            } catch(Exception e) {
                System.out.println("Exception :" + e.getMessage());
            }

//            delete product
            repo.delete(product);
        } catch(Exception e) {
            System.out.println("Exception :" + e.getMessage());
        }
        return "redirect:/products";
    }
}
