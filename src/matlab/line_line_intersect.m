function [S, t1, t2] = line_line_intersect(line1, line2)

P1 = line1(1:2);
Q1 = line1(3:4);
P2 = line2(1:2);
Q2 = line2(3:4);

R1 = Q1 - P1;
R2 = Q2 - P2;

% TODO: catch bad matrix condition (must likely due to parallel lines)
t = [-R1 R2] \ (P1 - P2);

t1 = t(1);
t2 = t(2);

S = P1 + t1*R1;
