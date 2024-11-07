# kotlin-convenience-store-precourse

###

- 사용자가 입력한 상품의 가격과 수량을 기반으로 최종 결제 금액을 계산한다.
- 총구매액은 상품별 가격과 수량을 곱하여 계산하며, 프로모션 및 멤버십 할인 정책을 반영하여 최종 결제 금액을 산출한다.
- 구매 내역과 산출한 금액 정보를 영수증으로 출력한다.
- 영수증 출력 후 추가 구매를 진행할지 또는 종료할지를 선택할 수 있다.
- 사용자가 잘못된 값을 입력할 경우 IllegalArgumentException를 발생시키고, "[ERROR]"로 시작하는 에러 메시지를 출력 후 그 부분부터 입력을 다시 받는다.
- Exception이 아닌 IllegalArgumentException, IllegalStateException 등과 같은 명확한 유형을 처리한다.

### 재고 관리 객체

- 각 상품의 재고 수량을 고려하여 결제 가능 여부를 확인한다.
- 고객이 상품을 구매할 때마다, 결제된 수량만큼 해당 상품의 재고에서 차감하여 수량을 관리한다.
- 재고를 차감함으로써 시스템은 최신 재고 상태를 유지하며, 다음 고객이 구매할 때 정확한 재고 정보를 제공한다.

### 프로모션 할인

- 오늘 날짜가 프로모션 기간 내에 포함된 경우에만 할인을 적용한다.
- 프로모션은 N개 구매 시 1개 무료 증정(Buy N Get 1 Free)의 형태로 진행된다.
- 1+1 또는 2+1 프로모션이 각각 지정된 상품에 적용되며, 동일 상품에 여러 프로모션이 적용되지 않는다.
- 프로모션 혜택은 프로모션 재고 내에서만 적용할 수 있다.
- 프로모션 기간 중이라면 프로모션 재고를 우선적으로 차감하며, 프로모션 재고가 부족할 경우에는 일반 재고를 사용한다.
- 프로모션 적용이 가능한 상품에 대해 고객이 해당 수량보다 적게 가져온 경우, 필요한 수량을 추가로 가져오면 혜택을 받을 수 있음을 안내한다.
- 프로모션 재고가 부족하여 일부 수량을 프로모션 혜택 없이 결제해야 하는 경우, 일부 수량에 대해 정가로 결제하게 됨을 안내한다.

### 멤버십 할인

- 멤버십 회원은 프로모션 미적용 금액의 30%를 할인받는다.
- 프로모션 적용 후 남은 금액에 대해 멤버십 할인을 적용한다.
- 멤버십 할인의 최대 한도는 8,000원이다.

### 영수증 출력

- 영수증은 고객의 구매 내역과 할인을 요약하여 출력한다.
- 영수증 항목은 아래와 같다.
- 구매 상품 내역: 구매한 상품명, 수량, 가격
- 증정 상품 내역: 프로모션에 따라 무료로 제공된 증정 상품의 목록
- 금액 정보
- 총구매액: 구매한 상품의 총 수량과 총 금액
- 행사할인: 프로모션에 의해 할인된 금액
- 멤버십할인: 멤버십에 의해 추가로 할인된 금액
- 내실돈: 최종 결제 금액
- 영수증의 구성 요소를 보기 좋게 정렬하여 고객이 쉽게 금액과 수량을 확인할 수 있게 한다.

# W 편의점 구매 흐름

---

## 1. 인사

- **안녕하세요. W편의점입니다.**
- 현재 보유하고 있는 상품을 안내합니다.

---

## 2. 인벤토리 상품 생성 및 상품 출력

```plaintext
현재 보유하고 있는 상품입니다.

- 콜라 1,000원 10개 탄산2+1
- 콜라 1,000원 10개
- 사이다 1,000원 8개 탄산2+1
- 사이다 1,000원 7개
- 오렌지주스 1,800원 9개 MD추천상품
- 오렌지주스 1,800원 재고 없음
- 탄산수 1,200원 5개 탄산2+1
- 탄산수 1,200원 재고 없음
- 물 500원 10개
- 비타민워터 1,500원 6개
- 감자칩 1,500원 5개 반짝할인
- 감자칩 1,500원 5개
- 초코바 1,200원 5개 MD추천상품
- 초코바 1,200원 5개
- 에너지바 2,000원 5개
- 정식도시락 6,400원 8개
- 컵라면 1,700원 1개 MD추천상품
- 컵라면 1,700원 10개
```

---

## 3. 구매할 상품 입력

- **구매할 상품명과 수량을 입력해 주세요.** (예: `[사이다-2], [감자칩-1]`)

  ### 3.1 형식 오류 시
    - `[ERROR] 올바르지 않은 형식으로 입력했습니다. 다시 입력해 주세요.`

---

## 4. 재고 확인

### 4.1 존재하지 않는 상품 입력 시

- `[ERROR] 존재하지 않는 상품입니다. 다시 입력해 주세요.`

### 4.2 구매 수량이 재고 초과 시

- `[ERROR] 재고 수량을 초과하여 구매할 수 없습니다. 다시 입력해 주세요.`

---

## 5. 상품 확인 및 추가 상품 여부 확인

### 5.1 프로모션 상품 여부 확인

### 5.2 프로모션 미적용 시

- 일반 구매 진행

### 5.3 프로모션 적용 시

- 적게 구매한 경우 추가 여부 확인:
    - **안내 메시지:** `현재 {상품명}은(는) 1개를 무료로 더 받을 수 있습니다. 추가하시겠습니까? (Y/N)`
        - **Y** -> 추가 시 구매 개수 증가
        - **N** -> 추가 없이 구매

---

## 6. 프로모션 재고 부족 확인

### 6.1 부족 시 일부 수량을 정가로 구매할지 확인

- **Y** -> 일부 수량만 정가로 계산
- **N** -> 정가로 결제하는 수량 제외

---

## 7. 멤버십 할인 여부 확인

- **Y** -> 멤버십 할인 적용
- **N** -> 멤버십 할인 미적용

---

## 8. 구매 상품 내역, 증정 상품 내역, 금액 정보 출력

```plaintext
=============== W 편의점 ===============
상품명           수량     금액
콜라            3       3,000
에너지바        5       10,000
=============== 증 정 =================
콜라            1
======================================
총구매액        8       13,000
행사할인               -1,000
멤버십할인             -3,000
내실돈                  9,000
```

## 9. 추가 구매 여부 확인

- **출력** : 감사합니다. 구매하고 싶은 다른 상품이 있나요? (Y/N)
- **Y** -> 1단계로 돌아감 (새로운 상품 구매)
- **N** -> 구매 종료 이거 마크다운 파일에서 가독성 좋게 사용 할 수 있도록 코드 구성해줘