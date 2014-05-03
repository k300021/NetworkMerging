NetworkMerging
==============

Reson why i construct this module:

There are problems when floodlight using topology mixed SDN and legacy switches.(There will be broadcast storm when mix them)

Hence, i create a algorithm and slove it by creating a new module in floodlight


==============

My test environment

Fedora 14 and network simulator Estinet


===============

How to test it:

first u need to install estinet and Fedora 14 (in my algorithm  it deals with STP in estinet different from spec 802.1D)

so if u try to use it in actually network , u have to change some codes.

second change the floodlight resource to include the new module (NetworkMerging)

and last enjoy it!!


============== Method ==============================
Merge the SDN network and legacy network using floodlight


First, catch the BPDU packet sended by legacy switch, and analyze it to find the information i need.

Secondenary ,reduce the multipath between SDN island and Legacy island (to only one path)

Third , eliminate loop in the topology by using DFS


