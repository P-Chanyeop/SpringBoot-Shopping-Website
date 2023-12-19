package com.example.shop.service;


import com.example.shop.entity.ItemImg;
import com.example.shop.repository.ItemImgRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import javax.persistence.EntityNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemImgService {

    @Value("${itemImgLocation}")
    private String itemImgLocation;

    private final ItemImgRepository itemImgRepository;

    private final FileService fileService;

    // 상품 이미지 등록 함수
    public void saveItemImg(ItemImg itemImg, MultipartFile itemImgFile) throws Exception{
        String oriImgName = itemImgFile.getOriginalFilename();
        String imgName = "";
        String imgUrl = "";

        // 파일 업로드
        if(!StringUtils.isEmpty(oriImgName)){
            imgName = fileService.uploadFile(itemImgLocation, oriImgName, itemImgFile.getBytes());

            imgUrl = "/images/item/" + imgName;
        }

        // 상품 이미지 정보 저장
        itemImg.updateItemImg(oriImgName, imgName, imgUrl);
        itemImgRepository.save(itemImg);
    }

    // 상품 수정 이미지 변경감지 함수
    public void updateItemImg(Long itemImgId, MultipartFile itemImgFile) throws Exception{

        if (!itemImgFile.isEmpty()) {   // 상품 이미지를 수정한 경우 상품 이미지 업데이트
            // 상품 이미지 아이디를 이용하여 기존에 저장했던 상품 이미지 엔티티 조회
            ItemImg savedItemImg = itemImgRepository.findById(itemImgId)
                    .orElseThrow(EntityNotFoundException::new);

            // 기존에 등록된 상품 이미지 파일이 있을경우 해당 파일을 삭제
            // 기존 이미지 파일 삭제
            if (!StringUtils.isEmpty(savedItemImg.getImgName())){
                fileService.deleteFile(itemImgLocation + "/" +
                        savedItemImg.getImgName());
            }

            String oriImgName = itemImgFile.getOriginalFilename();

            // 업데이트한 상품 이미지 파일 업로드
            String imgName = fileService.uploadFile(itemImgLocation,
                    oriImgName, itemImgFile.getBytes());

            String imgUrl = "/images/item/" + imgName;

            // 변경된 상품 이미지 정보를 세팅
            // 상품 등록 때처럼 itemImgRepository.save() 로직을 호출하는 것이 아닌,
            // savedItemImg 엔티티가 영속 상태이므로 데이터를 변경하는 것만으로
            // 데이터 변경 감지 기능이 동작하여 트랜잭션이 끝날 때 update 쿼리가 수행된다.
            // 여기서 중요한것은 엔티티가 영속 상태여야 한다는 것이다.
            savedItemImg.updateItemImg(oriImgName, imgName, imgUrl);
        }


    }
}
