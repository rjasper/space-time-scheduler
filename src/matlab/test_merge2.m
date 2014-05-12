
P = polygon_fixture;
P4 = P{4};

[C, vid_L, vid_R, vid_S] = cut_polygon(P4, 1:size(P4, 2), [0 0 1 0]');
V = [P4 C];

[V_, vid_] = merge_polygons(V, vid_L, vid_S, V, vid_R, vid_S);

draw_polygon(vid2polygon(V_, vid_), 'r');