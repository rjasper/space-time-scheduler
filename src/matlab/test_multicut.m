V = [
    -1 1 1 0 -1;
    -1 -1 1 0 1;
];

vid = 1:5;

cuts = [0 -1 0 1; -1 0 1 0]';

[C, vid_, vid_S, S_xT, B] = multicut_polygon(V, vid, cuts);