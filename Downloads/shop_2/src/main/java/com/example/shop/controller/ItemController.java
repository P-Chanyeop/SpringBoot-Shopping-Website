package com.example.shop.controller;

import com.example.shop.dto.ItemFormDto;
import com.example.shop.dto.ItemSearchDto;
import com.example.shop.entity.Item;
import com.example.shop.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Controller
public class ItemController {

    @Autowired
    private ItemService itemService;

    // 상품 등록 페이지 GET 요청 처리
    @GetMapping(value = "/admin/item/new")
    public String itemFrom(Model model){
        model.addAttribute("itemFormDto", new ItemFormDto());
        return "/item/itemForm";
    }

    // 상품 등록 POST 요청 처리
    @PostMapping(value = "/admin/item/new")
    public String itemNew(@Valid ItemFormDto itemFormDto, BindingResult bindingResult,
                          Model model, @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList){

        if (bindingResult.hasErrors()){
            return "item/itemForm";
        }

        // 대표이미지가 비어있을 경우 error 발생
        if(itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null){
            model.addAttribute("errorMessage", "첫번째 상품 이미지는 필수 입력 값입니다.");
            return "item/itemForm";
        }

        // 예외처리
        try{
           itemService.saveItem(itemFormDto, itemImgFileList);
        } catch (Exception e){
            model.addAttribute("errorMessage", "상품 등록 중 에러가 발생하였습니다.");
            return "item/itemForm";
        }

        // 상품 등록 성공 시 메인페이지로 리다이렉트
        return "redirect:/";
    }

    // 상품 수정 페이지 GET 요청 처리
    @GetMapping(value = "/admin/item/{itemId}")
    public String itemDtl(@PathVariable("itemId") Long itemId, Model model) {

        try{
            ItemFormDto itemFormDto = itemService.getItemDtl(itemId);
            model.addAttribute("itemFormDto", itemFormDto);
        } catch (EntityNotFoundException e){
            model.addAttribute("errorMessage", "존재하지 않는 상품입니다.");
            model.addAttribute("itemFormDto", new ItemFormDto());
            return "item/itemForm";
        }

        return "item/itemForm";
    }

    // 상품 수정 POST 요청 처리
    @PostMapping(value = "/admin/item/{itemId}")
    public String itemUpdate(@Valid ItemFormDto itemFormDto,
                             BindingResult bindingResult, @RequestParam("itemImgFile") List<MultipartFile>
                                     itemImgFileList, Model model) {

        if (bindingResult.hasErrors()) {
            return "item/itemForm";
        }

        // 똑같이 상품 대표 이미지가 없다면 에러 발생
        if (itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null){
            model.addAttribute("errorMessage", "첫번재 상품 이미지는 필수 입력 값입니다.");
            return "item/itemForm";
        }

        // 예외 처리
        try{
            // 상품 수정 비지니스 로직을 호출
            itemService.updateItem(itemFormDto, itemImgFileList);
        } catch (Exception e){
            model.addAttribute("errorMessage", "상품 수정 중 에러가 발생하였습니다.");
            return "item/itemForm";
        }

        // 상품 정상 수정시 메인페이지로 리다이렉트
        return "redirect:/";
    }

    // 상품 관리 화면 이동 및 조회한 상품 데이터를 화면에 전달하는 메소드
    // 상품 데이터가 많이 없기 때문에, 한 페이지당 총 3개의 상품만 보여줌.
    // 페이지 번호는 0부터 시작하는것에 유의
    // value에 상품 관리 화면 진입 시 URL에 페이지 번호가 없는 경우와 페이지 번호가 있는 경우 2가지를 매핑
    @GetMapping(value = {"/admin/items", "/admin/items/{page}"})
    public String itemManage(ItemSearchDto itemSearchDto,
                             @PathVariable("page") Optional<Integer> page, Model model){
        // 페이징을 위해서 PageRequest.of 메소드를 통해 Pageable 객체를 생성
        // 첫번째 파라미터는 조회할 페이지 번호, 두번째 파라미터는 한번에 가지고 올 데이터 수 를 나타냄.
        // URL 경로에 페이지 번호가 있으면 해당 페이지를 조회하도록 세팅하고 페이지 번호가 없으면 0페이지를 조회
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 3);

        // 조회 조건과 페이징 정보를 파라미터로 넘겨서 Page<Item> 객체를 반환 받는다
        Page<Item> items =
                itemService.getAdminItemPage(itemSearchDto, pageable);
        // 조회한 상품 데이터 및 페이징 정보를 뷰에 전달
        model.addAttribute("items", items);
        // 페이지 전환 시 기존 검색 조건을 유지한 채 이동할 수 있도록 뷰에 다시 전달
        model.addAttribute("itemSearchDto", itemSearchDto);
        // 상품 관리 메뉴 하단에 보여줄 페이지 번호의 최대 개수. 5로 설정했으니 최대 5개의 이동할 페이지 번호만 보여줌.
        model.addAttribute("maxPage", 5);
        return "item/itemMng";
    }

    // 상품 상세 페이지 매핑
    @GetMapping(value = "/item/{itemId}")
    public String itemDtl(Model model, @PathVariable("itemId") Long itemId){
        ItemFormDto itemFormDto = itemService.getItemDtl(itemId);
        model.addAttribute("item", itemFormDto);
        return "item/itemDtl";
    }

}
