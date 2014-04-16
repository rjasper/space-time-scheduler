function [S, t1, t2] = line_line_intersect(l1, l2)

P1 = l1(1:2);
Q1 = l1(3:4);
P2 = l2(1:2);
Q2 = l2(3:4);

R1 = Q1 - P1;
R2 = Q2 - P2;

% TODO: catch bad matrix condition (most likely due to parallel lines)
t = [-R1 R2] \ (P1 - P2);

t1 = t(1);
t2 = t(2);

S = P1 + t1*R1;
