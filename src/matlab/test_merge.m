
V1_L = [
    0 0 2 2 1 1 2 2;
    6 0 1 2 2 4 4 5;
];
vid1_L = {1:8};
vid1_S_L = [3 4 7 8];

V1_R = [
    2 2 4 2 2 4;
    5 4 4 2 1 2;
];
vid1_R = {1:3 4:6};
vid1_S_R = [5 4 2 1];

V2_L = [
    4 0 0 1 1 3;
    4 4 0 1 3 3;
];
vid2_L = {1:6};
vid2_S_L = [3 4 6 1];

V2_R = [
    0 4 4 3 3 1;
    0 0 4 3 1 1;
];
vid2_R = {1:6};
vid2_S_R = [1 6 4 3];

[V1, vid1] = merge_polygons(V1_L, vid1_L, vid1_S_L, V1_R, vid1_R, vid1_S_R);
[V2, vid2] = merge_polygons(V2_L, vid2_L, vid2_S_L, V2_R, vid2_R, vid2_S_R);

P1_L = vid2polygon(V1_L, vid1_L);
P1_R = vid2polygon(V1_R, vid1_R);
P2_L = vid2polygon(V2_L, vid2_L);
P2_R = vid2polygon(V2_R, vid2_R);

P1 = vid2polygon(V1, vid1);
P2 = vid2polygon(V2, vid2);

figure(1);
draw_polygon([P1_L P1_R], 'r');

figure(2);
draw_polygon([P2_L P2_R], 'r');

figure(3);
draw_polygon(P1, 'r');

figure(4);
draw_polygon(P2, 'r');