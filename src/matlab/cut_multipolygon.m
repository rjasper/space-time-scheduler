function [C, vid_L, vid_R, vid_S, S_xT, B, eid_C] = cut_multipolygon(V, vid_P, cut)

if isempty(vid_P)
    C = NaN(2, 0);
    vid_L = {};
    vid_R = {};
    vid_S = zeros(1, 0);
    S_xT = NaN(1, 0);
    B = false(2, 0);
    eid_C = zeros(2, 0);
    
    return;
end

% number of polygons
N_P = length(vid_P);

% number of vertices
n_V = size(V, 2);

% cut each polygon
[C, vid_L, vid_R, vid_S, S_xT, B, eid_C] = ...
    cellfun(@(vid) cut_polygon(V, vid, cut), vid_P, 'UniformOutput', false);

% number of crossed edges (new vertices)
n_C = cellfun(@(c) size(c, 2), C);
ncum_C = [0 cumsum( n_C(1:end-1) )];
ncum_C_ = num2cell( ncum_C );

% calculate new indices
vid_L = cellfun(@calc_index, vid_L, ncum_C_, 'UniformOutput', false);
vid_R = cellfun(@calc_index, vid_R, ncum_C_, 'UniformOutput', false);
vid_S = cellfun(@calc_index, vid_S, ncum_C_, 'UniformOutput', false);

% eid_C = cellfun( ...
%     @(pid, eid) [repmat(pid, length(eid), 1); ncum_C(pid) + eid], ...
%     num2cell( 1:N_P ), ...
%     eid_C, ...
%     'UniformOutput', false);
% eid_C = [eid_C{:}];

eid_C = unroll(eid_C);

[C, vid_L, vid_R, vid_S, S_xT, B] = ...
    cellflatten(C, vid_L, vid_R, vid_S, S_xT, B);

% sort shared points (points on the cut) by hit order
[S_xT, S_idx] = sort(S_xT);
vid_S = vid_S(S_idx);
B = B(:, S_idx);

    function vid_ = calc_index(vid, offset)
        if iscell(vid)
            vid_ = cellfun(@helper, vid, 'UniformOutput', false);
        else
            vid_ = helper(vid);
        end
        
        function vid_ = helper(vid)
            vid_ = NaN(size(vid));
            
            P_filt = vid <= n_V;
            C_filt = not(P_filt);
            vid_(P_filt) = vid(P_filt);
            vid_(C_filt) = vid(C_filt) + offset;
        end
    end
end