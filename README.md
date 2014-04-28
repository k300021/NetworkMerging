NetworkMerging
==============

Reson why i construct this module:

There are problems when floodlight using topology mixed SDN and legacy switches.(There will be broadcast storm when mix them)

Hence, i create a algorithm and slove it by creating a new module in floodlight


==============

My test environment

Fedora 14 and network simulator Estinet


===============





Merge the SDN network and legacy network using floodlight


First, catch the BPDU packet sended by legacy switch, and analyze it to find the information i need.

Secondenary , cut the redunant path by the information i have.
