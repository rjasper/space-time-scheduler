function [C, vid_, vid_S, S_xT, B] = multicut_polygon(V, vid, cuts)

n_cuts = size(cuts, 2);
n_V = size(V, 2);

C = cell(1, n_cuts);
vid_S = cell(1, n_cuts);
S_xT = cell(1, n_cuts);
B = cell(1, n_cuts);

%% cut polygon
% cut the polygon multiple times

V_ = V; % TODO: preallocate
vid_ = {vid};

for i = 1:n_cuts
    [C_i, vid_, ~, vid_S_i, S_xT_i, B_i] = cut_multipolygon(V_, vid_, cuts(:, i));
    
    V_ = [V_ C_i];
    C{i} = C_i;
    vid_S{i} = vid_S_i;
    S_xT{i} = S_xT_i;
    B{i} = B_i;
end

%% determine cut vertices which are still part of the polygon
% some initial shared vertices might have been cut off by following cuts
% determine the shared vertices which are still present

S_filt = cell(1, n_cuts-1);
isB = cellfun(@(b) b(1, :) & b(2, :), B, 'UniformOutput', false);

for i = 1:n_cuts-1
    S = V_(:, vid_S{i});
    y_T = transform_y(S, cuts(:, i+1));
    
    S_filt{i} = y_T >= 0; % TODO: use epsilon
end

% the first cut may lose initial shared points on both sides
% while the last cut does not lose any shared points

S1 = V_(:, vid_S{1});
yn_T = transform_y(S1, cuts(:, n_cuts));
S_filt{1}(yn_T < 0) = false;

%% determine cut vertices which are part of multiple cuts
% some shared vertices might be part of multiple cuts

% check the common point of the first and the last cut

idx_S1_first = find(S_filt{1}, 1, 'first');
is_first_bridge_cut = ~isempty(idx_S1_first) && idx_S1_first > 1 && isB{1}(idx_S1_first-1);

if is_first_bridge_cut
    vid1_S_first = vid_S{end}(end);
    S1_xT_first = transform_x( V_(:, vid1_S_first), cuts(:, 1) );
    B1_first = B{1}(:, idx_S1_first-1);
end

% filter all initial shared points which are not part of the polygon
% anymore

[vid_S_, S_xT_, B_] = cellfun( ...
    @filter_array, ...
    S_filt, vid_S(1:end-1), S_xT(1:end-1), B(1:end-1), ...
    'UniformOutput', false);
vid_S(1:end-1) = vid_S_;
S_xT(1:end-1) = S_xT_;

B_ = cellfun(@(b) [b(:, 1:end-1) false(2, 1)], B_, 'UniformOutput', false);
B(1:end-1) = B_;

% add common points between all cuts except first and last

vid_S_first = cellfun(@(v) first(v, 0), vid_S(2:end));

for i = 1:n_cuts-1
    idx_S_last = find(S_filt{i}, 1, 'last');
    
    if isempty(idx_S_last)
        continue;
    end
    
    if isB{i}(:, idx_S_last) && vid_S{i}(idx_S_last) ~= vid_S_first(i)
        vid_S{i}(end+1) = vid_S_first(i);
        S_xT {i}(end+1) = transform_x( V_(:, vid_S_first(i)), cuts(:, i) );
    end
end

% add the common point of the first and last cut

if is_first_bridge_cut
    vid_S{1} = [vid1_S_first vid_S{1}];
    S_xT{1} = [S1_xT_first S_xT{1}];
    B{1} = [B1_first B{1}];
end

% remove initial crossing points which were cut off and remap indices

[C, vid_S, ~, vid_] = vid_remap([C{:}], vid_S, n_V, vid_);

end

function x_T = transform_x(pts, cut)

n_pts = size(pts, 2);
vec = calc_line_vec(cut);
l = norm(vec);
R_y = [vec(1) vec(2)] / l;

x_T = R_y * (pts - repmat(cut(1:2), 1, n_pts));

end

function y_T = transform_y(pts, cut)

n_pts = size(pts, 2);
vec = calc_line_vec(cut);
l = norm(vec);
R_y = [-vec(2) vec(1)] / l;

y_T = R_y * (pts - repmat(cut(1:2), 1, n_pts));

end

function id = first(ids, default)

if isempty(ids)
    id = default;
else
    id = ids(:, 1);
end

end

function varargout = filter_array(filter, varargin)

n_filter = length(filter);
n_filter_ = sum(filter);

varargout = cellfun(@helper, varargin, 'UniformOutput', false);

    function arr_ = helper(arr)
        size_arr = size(arr);
        
        arr_ = reshape(arr, [], n_filter);
        arr_ = arr_(:, filter);
        arr_ = reshape(arr_, [size_arr(1:end-1) n_filter_]);
    end

end