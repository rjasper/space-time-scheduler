function [P_L, P_R, V, vid_L, vid_R] = cut_polygon(P, cut)

% catch all 'r' or all 'l'

n = size(P, 2);
e = get_edges(P);

P_ = transform(P, cut);

% determine side of P's vertices
s = arrayfun(@vertex_side, P_(2, :));

% TODO: check result variables
if all(s == 'l')
    P_L = {P};
    P_R = {};
    V = P;
    vid_L = {1:n};
    vid_R = {};
    
    return;
elseif all(s == 'r')
    P_L = {};
    P_R = {P};
    V = P;
    vid_L = {};
    vid_R = {1:n};
    
    return;
end

[C, C_eid, n_C] = calculate_crossings(e, cut, s);
C_ = transform(C, cut); % TODO: recalculates R-matrix

% s_ = [s s(1)];
% 
% % find edge crossings with cut
% C_eid = [ strfind(s_, 'lr'), strfind(s_, 'rl') ]; % cutted edges (id)
% n_C = length(C_eid); % number of edge cuts
% 
% % calculate crossing points
% C = NaN(2, n_C);
% C_vid = NaN(1, n_C);
% C_neighbors = NaN(2, n_C);
% for i = 1:n_C
%     eid = C_eid(i);
%     pred = mod(eid   - 1, n) + 1;
%     succ = mod(eid+1 - 1, n) + 1;
%     
%     C(:, i) = line_line_intersect(cut, e(:, eid));
%     C_vid(i) = n + i;
%     C_neighbors(:, i) = [pred; succ];
% end
% C_ = R * (C - repmat(L1, 1, n_C));

% merge polygon vertices and crossing points
V  = [P C];
V_ = [P_ C_];
s  = [s repmat('b', 1, n_C)];
n_V = size(V, 2);

% TODO: C_vid unused
% reorder vertices
[reorder_idx, C_vid] = calculate_vertex_order(C_eid, n);
V  = V(:, reorder_idx);
V_ = V_(:, reorder_idx);
s  = s(:, reorder_idx);

% determine polygon vertices on the cut
b_filt = s == 'b';
b_vid = find(b_filt);
n_C = size(b_vid, 2);

% sort cuts by 'hit order'
[~, hit_idx] = sort(V_(1, b_filt), 2);
C_vid = b_vid(hit_idx);
C  = V(C_vid);
C_ = V_(C_vid);

% n_b = sum(b_filt);
% C = [C P(:, b_filt)];
% C_ = [C_ P_(:, b_filt)];
% C_vid = [C_vid b_vid];
% n_C = n_C + n_b;
% s = [s repmat('b', 1, n_b)];
% 
% b_pred = mod(b_vid-1 - 1, n) + 1;
% b_succ = mod(b_vid+1 - 1, n) + 1;
% C_neighbors = [C_neighbors [b_pred; b_succ]];
% 
% % sort cuts by 'hit order'
% [~, hit_idx] = sort(C_(1, :), 2);
% C = C(:, hit_idx);
% C_vid = C_vid(hit_idx);
% % C_neighbors = C_neighbors(:, idx);

C_dir = determine_direction(C_vid, V_, s);
B = cellfun(@(d, id) determine_bridge_passing(d, id, V_), ...
    mat2cell(C_dir, 2, ones(1, n_C)), ...
    num2cell(C_vid), ...
...%     mat2cell(C_neighbors, 2, ones(1, n_C)), ...
    'UniformOutput', false);
B = [B{:}];

% number of polygons after split
N = 1 + length( strfind(B(1, :) & B(2, :), [true false]) ) ...
    + sum( ismember(C_dir', 'll', 'rows')' & B(1, :) ) ...
    + sum( ismember(C_dir', 'rr', 'rows')' & B(2, :) );


used_vertex = false(1, n_V);
vid_cut = cell(1, N);
vid_cut_i = NaN(1, n + N-1);
i = 1;
% for each polygon piece
while ~all(used_vertex | b_filt)
%     disp( sum(not(used_vertex | b_filt)) );
    % each non-crossing vertex is used once
    vid_first = find(~used_vertex & ~b_filt, 1, 'first');
    
    if vid_first == 1 && s(end) == s(1)
        vid_first = find(s ~= s(1), 1, 'last') + 1;
    end
    
    cur = vid_first;
    side = s(cur);
    no_loop = true;
    j = 1;
    
    % for each contiguous range of non-crossing vertices of the current
    % polygon piece
    while no_loop
        % get last vertex of contiguous non-crossing vertices
        last = find_last_contiguous(s, cur, 'x');
        
        I = interval(cur, last, n_V);
        n_I = size(I, 2);
        I_ = (1:n_I) + j-1;
        
        vid_cut_i(I_) = I;
        used_vertex(I) = true;
        j = j + n_I;
        
%         disp(I);
        
        [~, cur] = neighbors(last, n_V); % crossing point
        cid = find(C_vid == cur, 1, 'first');
        
        switch side
            case 'l'
                upward_cid = upward(cid, B);
                I_cid = cid:upward_cid;
            case 'r'
                downward_cid = downward(cid, B);
                I_cid = cid:-1:downward_cid;
        end
        
        I = C_vid(I_cid);
        
        last_idx = find( I == prev(vid_first, n_V), 1, 'first' );
        if ~isempty(last_idx)
            I = I(1:last_idx);
        end
        
        n_I = size(I, 2);
        I_ = (1:n_I) + j-1;
        
%         disp(I);
        
        vid_cut_i(I_) = I;
        j = j + n_I;
        
%         disp(vid_cut_i);
        
        cur = next(I(end), n_V);
        
        % detect loop
        no_loop = cur ~= vid_first;
    end
    
    % store polygon piece
    vid_cut{i} = vid_cut_i(:, 1:j-1);
    i = i + 1;
end

% disp(vid_cut);

% C_eid = C_eid(idx);

% % build new polygons after cut
% vid_cut = build_polygons(C_eid, s_, n, n_C);
% 
% % define new set of vertices
% V = [P C];
% 
% % split polygons by their side

% determine the polygons side (left or right)
s_P = cellfun(@(vid) polygon_side(vid, s), vid_cut);

vid_L = vid_cut(s_P == 'l');
vid_R = vid_cut(s_P == 'r');

P_L = vid2poly(vid_L, V);
P_R = vid2poly(vid_R, V);

% calculate P in a coordinate system where
% L1 is located in the origin and L2 lies on the positive x-axis
function P_ = transform(P, cut)

n = size(P, 2);
L1 = cut(1:2);
L2 = cut(3:4);

dL = L2 - L1;
l_length = sqrt( sum(dL.^2) );

% rotation matrix
R = [dL(1) dL(2); -dL(2) dL(1)] / l_length;
% transform P and l
P_ = R * (P - repmat(L1, 1, n));

function [pred_vid, succ_vid] = neighbors(vid, n)

pred_vid = mod( vid-1 - 1, n ) + 1;
succ_vid = mod( vid+1 - 1, n ) + 1;

function [C, C_eid, n_C] = calculate_crossings(e, cut, s)

s_ = [s s(1)];

% find edge crossings with cut
C_eid = sort([ strfind(s_, 'lr'), strfind(s_, 'rl') ]); % cutted edges (id)
n_C = length(C_eid); % number of edge cuts

% calculate crossing points
C = NaN(2, n_C);
% C_vid = NaN(1, n_C);
% C_neighbors = NaN(2, n_C);
for i = 1:n_C
    eid = C_eid(i);
%     pred = mod(eid   - 1, n) + 1;
%     succ = mod(eid+1 - 1, n) + 1;
    
    C(:, i) = line_line_intersect(cut, e(:, eid));
%     C_vid(i) = n + i;
%     C_neighbors(:, i) = [pred; succ];
end

function [idx, C_vid] = calculate_vertex_order(C_eid, n)

n_C = size(C_eid, 2);
n_V = n + n_C;

idx = NaN(1, n_V);
C_vid = NaN(1, n_C);

last = 0;
for i = 1:n_C
    C_eid_i = C_eid(i);
    C_vid_i = C_eid_i + i;
    I = last+1:C_eid_i;
    
    idx(I + i-1) = I;
    idx(C_vid_i) = n + i;
    C_vid(i) = C_vid_i;
    
    last = C_eid_i;
end

I = C_eid(end)+1:n;
idx(I + n_C) = I;

function idx = find_last_contiguous(arr, id0, delimiter)

n = size(arr, 2);
shifted = circshift(arr, [0 1-id0]);
kind = arr(id0);

idx_shifted = find( [shifted delimiter] ~= kind, 1, 'first' ) - 1;
idx = mod( idx_shifted + id0-1 - 1, n ) + 1;

function s = vertex_side(y)

if y < 0
    s = 'r'; % right
elseif y > 0
    s = 'l'; % left
else
    s = 'b'; % both left and right
end

function s = polygon_side(vid, s)

n = length(s);
s_vid = s(vid(vid <= n));

if any(s_vid == 'l')
    s = 'l'; % left
elseif any(s_vid == 'r')
    s = 'r'; % right
else
    s = 'b'; % both or middle
end

% function vid = cid2vid(cid, n)
% 
% vid = cid + n;

function P = vid2poly(vid, V)

P = cellfun(@(v) V(:, v), vid, 'UniformOutput', false);

function C_dir = determine_direction(C_vid, V_, s)

n_V = size(V_, 2);

[pred_vid, succ_vid] = neighbors(C_vid, n_V);

C_dir = s([pred_vid; succ_vid]);

b_filt = C_dir == 'b';
pred_b_idx = find(b_filt(1, :));
succ_b_idx = find(b_filt(2, :));

pred_b_vid = pred_vid(b_filt(1, :));
succ_b_vid = succ_vid(b_filt(2, :));

C_ = V_(:, C_vid);
pred_b_lower = V_(1, pred_b_vid) < C_(1, b_filt(1, :));
succ_b_lower = V_(1, succ_b_vid) < C_(1, b_filt(2, :));

C_dir(1, pred_b_idx( pred_b_lower)) = 'u'; % up
C_dir(1, pred_b_idx(~pred_b_lower)) = 'd'; % down
C_dir(2, succ_b_idx( succ_b_lower)) = 'd'; % down
C_dir(2, succ_b_idx(~succ_b_lower)) = 'u'; % up

function B = determine_bridge_passing(C_dir, C_vid, V_)

pred = C_dir(1);
succ = C_dir(2);

if succ == 'u'
    B = [true; false];
elseif pred == 'd'
    B = [false; true];
elseif (pred == 'u' || pred == 'r') && (succ == 'd' || succ == 'l')
    B = [false; false];
elseif (pred == 'u' || pred == 'l') && (succ == 'd' || succ == 'r')
    B = [true; true];
else % 'rr' or 'll'
    n_V = size(V_, 2);
    [pred_vid, succ_vid] = neighbors(C_vid, n_V);
    
    C_ = V_(:, C_vid);
    C_pred_ = V_(:, pred_vid);
    C_succ_ = V_(:, succ_vid);
    
    delta1 = C_ - C_pred_;
    delta2 = C_succ_ - C_;
    
%     delta1 = mat2cell(C - C_pred, [1 1]);
%     delta2 = mat2cell(C_succ - C, [1 1]);
%     
%     % TODO: ratios would be sufficient
%     
%     alpha1 = atan2(delta1{:});
%     alpha2 = atan2(delta2{:});
%     % note: no mod here since alphas have no common sign and their absolute
%     % value is lower than pi
%     dalpha = alpha2 - alpha1;
%     
%     if dalpha < 0
%         B = [false; false];
%     else % dalpha > 0
%         B = [true; true];
%     end
    
    m1 = -delta1(1)/delta1(2); % -cot(alpha1)
    m2 = -delta2(1)/delta2(2); % -cot(alpha2)
    
    if m1 > m2
        B = [false; false];
    else % m1 < m2
        B = [true; true];
    end
end

% function vid_cut = build_polygons(C_eid, s_, n, n_C)
% 
% % TODO: consider 'b's
% n_cut = n_C / 2 + 1; % number of polygons after cut
% 
% used_vertex = false(1, n);
% vid_cut = cell(1, n_cut);
% vid_cut_i = NaN(1, n);
% i = 1;
% % for each polygon piece
% while ~all(used_vertex)
%     % each original vertex is used once
%     vid_first = find(~used_vertex, 1, 'first');
%     cur = vid_first;
%     no_loop = true;
%     j = 1;
%     
%     % for each original vertex of the current polygon piece
%     while no_loop
%         vid_cut_i(j) = cur;
%         used_vertex(cur) = true;
%         j = j + 1;
%         
%         % test if the next vertex is a bridge (i.e., not original)
%         s_tmp = s_([cur cur+1]);
%         if strcmp(s_tmp, 'lr') || strcmp(s_tmp, 'rl') % is bridge
%             % a bridge connects to cutted edges
%             
%             % get the index of the cutted edge
%             cid = find(C_eid == cur, 1, 'first');
%             % get the index of the other cutted edge of the bridge
%             if mod(cid, 2) == 1 % if cid is odd
%                 cid_next = cid + 1;
%             else
%                 cid_next = cid - 1;
%             end
%             
%             % append the bridge vertices
%             vid_cut_i(j:j+1) = cid2vid([cid cid_next], n);
%             % store the next original vertex of the bridge's other side
%             vid_next = C_eid(cid_next) + 1;
%             j = j + 2;
%         else % is original vertex
%             if cur == n % consider wrap around
%                 vid_next = 1;
%             else
%                 vid_next = cur + 1;
%             end
%         end
%         
%         % detect loop
%         no_loop = vid_next ~= vid_first;
%         cur = vid_next;
%     end
%     
%     % store polygon piece
%     vid_cut{i} = vid_cut_i(:, 1:j-1);
%     i = i + 1;
% end

function I = interval(first, last, n)

if first <= last
    I = first:last;
else
    I = [first:n 1:last];
end

function upward_cid = upward(cid, B)

upward_cid = find( ~B(1, cid:end), 1, 'first' ) + cid-1;

function downward_cid = downward(cid, B)

downward_cid = find( [true ~B(2, 1:cid-1)], 1, 'last' );

function next_vid = next(vid, n)

if vid == n
    next_vid = 1;
else
    next_vid = vid+1;
end

function prev_vid = prev(vid, n)

if vid == 1
    prev_vid = n;
else
    prev_vid = vid-1;
end