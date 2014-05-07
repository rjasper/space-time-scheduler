function [V, vid] = merge_polygons(V_L, vid_L, vid_S_L, V_R, vid_R, vid_S_R)

N_P_max = length(vid_L) + length(vid_R);
n_P_max = sum([ cellfun(@length, vid_L) cellfun(@length, vid_R) ]);
n_S = length(vid_S_L); % == length(vid_S_R)

vid = cell(1, N_P_max);

[isS_L, mapTo_L] = cellfun(@(vid) ismember(vid, vid_S_L), vid_L, 'UniformOutput', false);
[isS_R, mapTo_R] = cellfun(@(vid) ismember(vid, vid_S_R), vid_R, 'UniformOutput', false);


vid_i = zeros(1, n_P_max);