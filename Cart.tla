----------------------------- MODULE Cart -----------------------------
EXTENDS Naturals, Sequences, FiniteSets, TLC

CONSTANTS 
    Items, 
    Prices, 
    MaxQty

DefaultPrices ==[i \in Items |-> IF i = "A" THEN 100 ELSE 200]

VARIABLES 
    qty,          \* số lượng từng item
    total,        \* tổng tiền cuối cùng
    last_action   \* Lưu lại vết hành động để sinh Test Case

vars == <<qty, total, last_action>>

(* ================= INIT ================= *)
Init ==
    /\ qty = [i \in Items |-> 0]
    /\ total = 0
    /\ last_action = <<"Khoi Tao">>

(* ================= HELPER ================= *)
RECURSIVE ComputeTotalRec(_, _)
ComputeTotalRec(q, S) == 
    IF S = {} THEN 0 
    ELSE LET i == CHOOSE x \in S: TRUE 
         IN (q[i] * Prices[i]) + ComputeTotalRec(q, S \ {i})
         
ComputeTotal(q) == ComputeTotalRec(q, Items)

(* ================= 1. CÁC HÀNH ĐỘNG TUẦN TỰ (PASS) ================= *)
Safe_AddItem(i) ==
    /\ qty[i] < MaxQty
    /\ qty' = [qty EXCEPT ![i] = @ + 1]
    /\ total' = ComputeTotal(qty')
    /\ last_action' = <<"TUAN_TU", "Them mon", i>>

Safe_RemoveItem(i) ==
    /\ qty[i] > 0
    /\ qty' =[qty EXCEPT ![i] = @ - 1]
    /\ total' = ComputeTotal(qty')
    /\ last_action' = <<"TUAN_TU", "Xoa mon", i>>

Safe_UpdateQuantity(i, n) ==
    /\ n \in 0..MaxQty
    /\ qty' = [qty EXCEPT ![i] = n]
    /\ total' = ComputeTotal(qty')
    /\ last_action' = <<"TUAN_TU", "Cap nhat so luong", i, n>>

(* ================= 2. CÁC HÀNH ĐỘNG SONG SONG LỖI (FAIL) ================= *)

\* 2.1: Thêm A + Thêm B cùng lúc
Race_AddA_AddB(i, j) ==
    /\ i /= j
    /\ qty[i] < MaxQty /\ qty[j] < MaxQty
    /\ qty' = [qty EXCEPT ![i] = @ + 1, ![j] = @ + 1]
    \* LỖI: Luồng Thêm B chạy chậm, đè mất kết quả tính tiền của Thêm A
    /\ total' = ComputeTotal([qty EXCEPT ![j] = @ + 1])
    /\ last_action' = <<"SONG_SONG_LOI", "Them A va Them B", i, j>>

\* 2.2: Thêm A + Xóa B cùng lúc
Race_AddA_RemoveB(i, j) ==
    /\ i /= j
    /\ qty[i] < MaxQty /\ qty[j] > 0
    /\ qty' =[qty EXCEPT ![i] = @ + 1, ![j] = @ - 1]
    \* LỖI: Luồng Xóa B ghi đè, làm mất kết quả của việc Thêm A
    /\ total' = ComputeTotal([qty EXCEPT ![j] = @ - 1])
    /\ last_action' = <<"SONG_SONG_LOI", "Them A va Xoa B", i, j>>

\* 2.3: Xóa A + Xóa B cùng lúc
Race_RemoveA_RemoveB(i, j) ==
    /\ i /= j
    /\ qty[i] > 0 /\ qty[j] > 0
    /\ qty' = [qty EXCEPT ![i] = @ - 1, ![j] = @ - 1]
    \* LỖI: Luồng Xóa B ghi đè, làm mất kết quả của việc Xóa A
    /\ total' = ComputeTotal([qty EXCEPT ![j] = @ - 1])
    /\ last_action' = <<"SONG_SONG_LOI", "Xoa A va Xoa B", i, j>>

\* 2.4: Hai người dùng cùng thêm một sản phẩm
Race_ConcurrentAddSameItem(i) ==
    /\ qty[i] < MaxQty - 1
    /\ qty' = [qty EXCEPT ![i] = @ + 1]
    \* LỖI: Cả hai luồng cùng đọc số lượng cũ, nên chỉ tăng được 1 đơn vị thay vì 2
    /\ total' = ComputeTotal([qty EXCEPT ![i] = @ + 1])
    /\ last_action' = <<"SONG_SONG_LOI", "2 nguoi dung cung Them mon", i>>

(* ================= NEXT ================= *)
Next ==
    \/ \E i \in Items: Safe_AddItem(i)
    \/ \E i \in Items: Safe_RemoveItem(i)
    \/ \E i \in Items, n \in 0..MaxQty: Safe_UpdateQuantity(i, n)
    \/ \E i \in Items, j \in Items: Race_AddA_AddB(i, j)
    \/ \E i \in Items, j \in Items: Race_AddA_RemoveB(i, j)
    \/ \E i \in Items, j \in Items: Race_RemoveA_RemoveB(i, j)
    \/ \E i \in Items: Race_ConcurrentAddSameItem(i)

(* ================= INVARIANT TẠO TEST CASE ================= *)
TotalCorrect ==
    LET expectedTotal == ComputeTotal(qty)
        isPass == (total = expectedTotal)
    IN IF last_action /= <<"Khoi Tao">> THEN
       PrintT([
           Kiem_Thu     |-> IF isPass THEN "✅ PASS" ELSE "❌ FAIL",
           Ngu_Canh     |-> last_action,
           Gio_Hang_Moi |-> qty,
           Tien_Luu_Sai |-> total,
           Tien_Dung    |-> expectedTotal
       ]) /\ TRUE
       ELSE TRUE

(* ================= SPEC ================= *)
Spec ==
    Init /\ [][Next]_vars
=======================================================================