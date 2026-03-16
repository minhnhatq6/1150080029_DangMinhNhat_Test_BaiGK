---- MODULE CartRaceCondition ----
EXTENDS Integers, TLC

CONSTANTS ThreadA, ThreadB

Threads == {ThreadA, ThreadB}

VARIABLES
    cart_total,
    qty_laptop,
    qty_mouse,
    temp_total

price_laptop == 1000
price_mouse == 50

Init ==
    /\ cart_total = 1050
    /\ qty_laptop = 1
    /\ qty_mouse = 1
    /\ temp_total = [t \in Threads |-> 0]

AddLaptop(t) ==
    /\ temp_total' = [temp_total EXCEPT ![t] = cart_total]
    /\ qty_laptop' = qty_laptop + 1
    /\ cart_total' = temp_total[t] + price_laptop
    /\ UNCHANGED qty_mouse

AddMouse(t) ==
    /\ temp_total' = [temp_total EXCEPT ![t] = cart_total]
    /\ qty_mouse' = qty_mouse + 1
    /\ cart_total' = temp_total[t] + price_mouse
    /\ UNCHANGED qty_laptop

DecLaptop(t) ==
    /\ qty_laptop > 0
    /\ temp_total' = [temp_total EXCEPT ![t] = cart_total]
    /\ qty_laptop' = qty_laptop - 1
    /\ cart_total' = temp_total[t] - price_laptop
    /\ UNCHANGED qty_mouse

RemoveLaptop(t) ==
    /\ qty_laptop > 0
    /\ temp_total' = [temp_total EXCEPT ![t] = cart_total]
    /\ cart_total' = temp_total[t] - (qty_laptop * price_laptop)
    /\ qty_laptop' = 0
    /\ UNCHANGED qty_mouse

RemoveMouse(t) ==
    /\ qty_mouse > 0
    /\ temp_total' = [temp_total EXCEPT ![t] = cart_total]
    /\ cart_total' = temp_total[t] - (qty_mouse * price_mouse)
    /\ qty_mouse' = 0
    /\ UNCHANGED qty_laptop

Next ==
    \E t \in Threads :
        AddLaptop(t)
        \/ AddMouse(t)
        \/ DecLaptop(t)
        \/ RemoveLaptop(t)
        \/ RemoveMouse(t)

ConsistentTotal ==
    cart_total = qty_laptop * price_laptop + qty_mouse * price_mouse

Spec ==
    Init /\ [][Next]_<<cart_total, qty_laptop, qty_mouse, temp_total>>

====