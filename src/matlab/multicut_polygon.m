function [C, vid_, vid_S, S_xT, B] = multicut_polygon(V, vid, cuts)

n_cuts = size(cuts, 2);
n_V = size(V, 2);

C = cell(1, n_cuts);
vid_S = cell(1, n_cuts);
S_xT = cell(1, n_cuts);
% B = cell(1, n_cuts);
B = cell(1, n_cuts);

%% cut polygon
% cut the polygon multiple times

V_ = V; % TODO: preallocate
vid_ = {vid};

for i = 1:n_cuts
    [C_i, vid_i, ~, vid_S_i, S_xT_i, B_i, eid_C] = cut_multipolygon(V_, vid_, cuts(:, i));
    
    % split existing shared vertices and keep only the left ones
    vid_S(1:i-1) = split_shared_vertices( ...
        cuts(:, i), ...
        cuts(:, 1:i-1), ...
        repmat('l', 1, i-1), ...
        V_, ...
        vid_, ...
        vid_S(1:i-1), ...
        eid_C, ...
        B(1:i-1));
    
%     isB_i = B_i(1, :) & B_i(2, :);
    
    V_ = [V_ C_i];
    C{i} = C_i;
    vid_ = vid_i;
    vid_S{i} = vid_S_i;
    S_xT{i} = S_xT_i;
    B{i} = B_i;
%     B{i} = isB_i;
end

% remove initial crossing points which were cut off and remap indices

[C, vid_S, ~, vid_] = vid_remap([C{:}], vid_S, n_V, vid_);

end