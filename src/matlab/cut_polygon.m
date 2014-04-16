function [P_L, P_R, V, vid_L, vid_R] = cut_polygon(P, cut)

n = size(P, 2);

L1 = cut(1:2);
L2 = cut(3:4);
dL = L2 - L1;
l_length = sqrt( sum(dL.^2) );

e = get_edges(P);

% calculate P in a coordinate system where
% L1 is located in the origin and L2 lies on the positive x-axis

% rotation matrix
R = [dL(1) dL(2); -dL(2) dL(1)] / l_length;
% transform P and l
P_ = R * (P - repmat(L1, 1, n));

% determine side of P's vertices
s = arrayfun(@vertex_side, P_(2, :));
s_ = [s s(1)];

% find edge crossings with cut
C_eid = [ strfind(s_, 'lr'), strfind(s_, 'rl') ]; % cutted edges (id)
n_C = length(C_eid); % number of edge cuts

% calculate crossing points
C = NaN(2, n_C);
for i = 1:n_C
    eid = C_eid(i);
    C(:, i) = line_line_intersect(cut, e(:, eid));
end

% sort cuts by 'hit order'
[~, idx] = sort(C(1, :), 2);
C = C(:, idx);
C_eid = C_eid(idx);

% build new polygons after cut
vid_cut = build_polygons(C_eid, s_, n, n_C);

% define new set of vertices
V = [P C];

% split polygons by their side

% determine the polygons side (left or right)
s_P = cellfun(@(vid) polygon_side(vid, s), vid_cut);

vid_L = vid_cut(s_P == 'l');
vid_R = vid_cut(s_P == 'r');

P_L = vid2poly(vid_L, V);
P_R = vid2poly(vid_R, V);

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
    s = 'l';
elseif any(s_vid == 'r')
    s = 'r';
else
    s = 'b';
end

function vid = cid2vid(cid, n)

vid = cid + n;

function P = vid2poly(vid, V)

P = cellfun(@(v) V(:, v), vid, 'UniformOutput', false);

function vid_cut = build_polygons(C_eid, s_, n, n_C)

% TODO: consider 'b's
n_cut = n_C / 2 + 1; % number of polygons after cut

used_vertex = false(1, n);
vid_cut = cell(1, n_cut);
vid_cut_i = NaN(1, n);
i = 1;
% for each polygon piece
while ~all(used_vertex)
    % each original vertex is used once
    vid_first = find(~used_vertex, 1, 'first');
    cur = vid_first;
    no_loop = true;
    j = 1;
    
    % for each original vertex of the current polygon piece
    while no_loop
        vid_cut_i(j) = cur;
        used_vertex(cur) = true;
        j = j + 1;
        
        % test if the next vertex is a bridge (i.e., not original)
        s_tmp = s_([cur cur+1]);
        if strcmp(s_tmp, 'lr') || strcmp(s_tmp, 'rl') % is bridge
            % a bridge connects to cutted edges
            
            % get the index of the cutted edge
            cid = find(C_eid == cur, 1, 'first');
            % get the index of the other cutted edge of the bridge
            if mod(cid, 2) == 1 % if cid is odd
                cid_next = cid + 1;
            else
                cid_next = cid - 1;
            end
            
            % append the bridge vertices
            vid_cut_i(j:j+1) = cid2vid([cid cid_next], n);
            % store the next original vertex of the bridge's other side
            vid_next = C_eid(cid_next) + 1;
            j = j + 2;
        else % is original vertex
            if cur == n % consider wrap around
                vid_next = 1;
            else
                vid_next = cur + 1;
            end
        end
        
        % detect loop
        no_loop = vid_next ~= vid_first;
        cur = vid_next;
    end
    
    % store polygon piece
    vid_cut{i} = vid_cut_i(:, 1:j-1);
    i = i + 1;
end