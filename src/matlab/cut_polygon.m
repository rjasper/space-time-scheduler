function [P_L, P_R, V, vid_L, vid_R, vid_B] = cut_polygon(P, cut)

P_L = {};
P_R = {};
vid_L = {};
vid_R = {};
vid_B = [];

n = size(P, 2);
e = get_edges(P);

P_ = transform(P);

% determine precision used by vertex_side
eps = determine_eps;
% determine side of P's vertices
s = arrayfun(@vertex_side, P_(2, :));
    
% TODO: check result variables
if all(s == 'l')
    P_L = {P};
    V = P;
    vid_L = {1:n};
    
    return;
elseif all(s == 'r')
    P_R = {P};
    V = P;
    vid_R = {1:n};
    
    return;
end

[C, C_eid, n_C] = calculate_crossings;
C_ = transform(C); % TODO: recalculates R-matrix

% merge polygon vertices and crossing points
V  = [P C];
V_ = [P_ C_];
s  = [s repmat('b', 1, n_C)];
n_V = size(V, 2);

% TODO: C_vid unused
% reorder vertices
reorder_idx = calculate_vertex_order;
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
C  = V(:, C_vid);
C_ = V_(:, C_vid);

C_dir = determine_direction;
B = cellfun(@determine_bridge_passing, ...
    mat2cell(C_dir, 2, ones(1, n_C)), ...
    num2cell(C_vid), ...
    'UniformOutput', false);
B = [B{:}];

% number of polygons after split
N = 1 + length( strfind(B(1, :) & B(2, :), [true false]) ) ...
    + sum( ismember(C_dir', 'll', 'rows')' & B(1, :) ) ...
    + sum( ismember(C_dir', 'rr', 'rows')' & B(2, :) );

% build independent polygon pieces
vid_cut = build_polygons;

% determine the polygons side (left or right)
s_P = cellfun(@polygon_side, vid_cut);

% devide pieces by their side
vid_L = vid_cut(s_P == 'l');
vid_R = vid_cut(s_P == 'r');

% determine the set of vertices used by both sides
vid_B = determine_shared_vertices;

% build actual polygons consisting of points
P_L = vid2poly(vid_L);
P_R = vid2poly(vid_R);

    function eps = determine_eps
        l = sqrt( sum(diff([P P(:, 1)], 1, 2).^2) );
        eps = min(l(l > 0)) * 1e-10;
    end

    % calculate P in a coordinate system where
    % L1 is located in the origin and L2 lies on the positive x-axis
    function pts_ = transform(pts)
        n_P = size(pts, 2);
        L1 = cut(1:2);
        L2 = cut(3:4);

        dL = L2 - L1;
        l_length = sqrt( sum(dL.^2) );

        % rotation matrix
        R = [dL(1) dL(2); -dL(2) dL(1)] / l_length;
        % transform P and l
        pts_ = R * (pts - repmat(L1, 1, n_P));
    end

    function [pred_vid, succ_vid] = neighbors(vid)
        pred_vid = mod( vid-1 - 1, n_V ) + 1;
        succ_vid = mod( vid+1 - 1, n_V ) + 1;
    end

    function [C, C_eid, n_C] = calculate_crossings
        % find edge crossings with cut
        s_ = [s s(1)];
        C_eid = sort( [strfind(s_, 'lr') strfind(s_, 'rl')] ); % cutted edges (id)
        n_C = length(C_eid); % number of edge cuts

        % calculate crossing points
        C = NaN(2, n_C);
        for i = 1:n_C
            eid = C_eid(i);
            C(:, i) = line_line_intersect(cut, e(:, eid));
        end
    end

    function idx = calculate_vertex_order
        idx = NaN(1, n_V);

        last = 0;
        for i = 1:n_C
            C_eid_i = C_eid(i);
            C_vid_i = C_eid_i + i;
            I = last+1:C_eid_i;

            idx(I + i-1) = I;
            idx(C_vid_i) = n + i;

            last = C_eid_i;
        end

        I = C_eid(end)+1:n;
        idx(I + n_C) = I;
    end

    function s = vertex_side(y)
        if y < -eps
            s = 'r'; % right
        elseif y > eps
            s = 'l'; % left
        else
            s = 'b'; % both left and right
        end
    end

    function s_P = polygon_side(vid)
        s_vid = s(vid);

        if any(s_vid == 'l')
            s_P = 'l'; % left
        elseif any(s_vid == 'r')
            s_P = 'r'; % right
        else
            s_P = 'b'; % both or middle
        end
    end

    function P = vid2poly(vid)
        P = cellfun(@(v) V(:, v), vid, 'UniformOutput', false);
    end

    function vid_B = determine_shared_vertices
        B_ = [false(2, 1) B];
        
        tmp = B_(:, 1:end-1) | B_(:, 2:end);
        vid_B = C_vid( tmp(1, :) & tmp(2, :) );
    end

    function C_dir = determine_direction
        [pred_vid, succ_vid] = neighbors(C_vid);

        C_dir = s([pred_vid; succ_vid]);

        filt = C_dir == 'b';
        pred_filt_idx = find(filt(1, :));
        succ_filt_idx = find(filt(2, :));

        pred_filt_vid = pred_vid(filt(1, :));
        succ_filt_vid = succ_vid(filt(2, :));

        pred_lower = V_(1, pred_filt_vid) < C_(1, filt(1, :));
        succ_lower = V_(1, succ_filt_vid) < C_(1, filt(2, :));

        C_dir(1, pred_filt_idx( pred_lower)) = 'u'; % up
        C_dir(1, pred_filt_idx(~pred_lower)) = 'd'; % down
        C_dir(2, succ_filt_idx( succ_lower)) = 'd'; % down
        C_dir(2, succ_filt_idx(~succ_lower)) = 'u'; % up
    end

    function B = determine_bridge_passing(dir, vid)
        pred = dir(1);
        succ = dir(2);

        if succ == 'u'
            B = [true; false];
        elseif pred == 'd'
            B = [false; true];
        elseif (pred == 'u' || pred == 'r') && (succ == 'd' || succ == 'l')
            B = [false; false];
        elseif (pred == 'u' || pred == 'l') && (succ == 'd' || succ == 'r')
            B = [true; true];
        else % 'rr' or 'll'
            [pred_vid, succ_vid] = neighbors(vid);

            C_cur_  = V_(:, vid);
            C_pred_ = V_(:, pred_vid);
            C_succ_ = V_(:, succ_vid);

            delta1 = C_cur_ - C_pred_;
            delta2 = C_succ_ - C_cur_;

            m1 = -delta1(1)/delta1(2); % -cot(alpha1)
            m2 = -delta2(1)/delta2(2); % -cot(alpha2)

            if m1 > m2
                B = [false; false];
            else % m1 < m2
                B = [true; true];
            end
        end
    end

    function vid_cut = build_polygons
        used_vertex = false(1, n_V);
        vid_cut = cell(1, N);
        vid_cut_i = NaN(1, n + N-1);
        i = 1;
        % for each polygon piece
        while ~all(used_vertex | b_filt)
            % each non-crossing vertex is used once
            vid_first = find(~used_vertex & ~b_filt, 1, 'first');

            if vid_first == 1 && s(end) == s(1)
                vid_first = find(s ~= s(1), 1, 'last') + 1;
            end

            cur = vid_first; % the first vertex of the polygon
            side = s(cur); % the side of the polygon (left or right)
            no_loop = true; % indicates whether the polygon is complete
            j = 1; % current vertex' index

            % for each contiguous range of non-crossing vertices of the current
            % polygon piece
            while no_loop
                % get last vertex of contiguous non-crossing vertices
                last = find_last_contiguous(s, cur, 'x');

                I = interval(cur, last);

                % append interval to the vid list
                n_I = size(I, 2);
                I_ = (1:n_I) + j-1;
                vid_cut_i(I_) = I;
                used_vertex(I) = true;
                j = j + n_I;

                % determine path along the bridges

                [~, cur] = neighbors(last); % crossing point
                cid = find(C_vid == cur, 1, 'first');

                % go either up or downward the bridges
                switch side
                    case 'l'
                        upward_cid = upward(cid);
                        I_cid = cid:upward_cid;
                    case 'r'
                        downward_cid = downward(cid);
                        I_cid = cid:-1:downward_cid;
                end

                I = C_vid(I_cid);

                % check if the bridges go beyond the last point
                last_idx = find( I == prev(vid_first), 1, 'first' );
                if ~isempty(last_idx)
                    I = I(1:last_idx);
                end

                % append interval to the vid list
                n_I = size(I, 2);
                I_ = (1:n_I) + j-1;
                vid_cut_i(I_) = I;
                j = j + n_I;

                cur = next(I(end));
                % detect loop
                no_loop = cur ~= vid_first;
            end

            % store polygon piece
            vid_cut{i} = vid_cut_i(:, 1:j-1);
            i = i + 1;
        end

        function I = interval(first, last)
            if first <= last
                I = first:last;
            else
                I = [first:n_V 1:last];
            end
        end

        function upward_cid = upward(cid)
            upward_cid = find( ~B(1, cid:end), 1, 'first' ) + cid-1;
        end

        function downward_cid = downward(cid)
            downward_cid = find( [true ~B(2, 1:cid-1)], 1, 'last' );
        end

        function next_vid = next(vid)
            if vid == n_V
                next_vid = 1;
            else
                next_vid = vid+1;
            end
        end

        function prev_vid = prev(vid)
            if vid == 1
                prev_vid = n_V;
            else
                prev_vid = vid-1;
            end
        end
    end
end

function idx = find_last_contiguous(arr, id0, delimiter)
    n_arr = size(arr, 2);
    shifted = circshift(arr, [0 1-id0]);
    kind = arr(id0);

    idx_shifted = find( [shifted delimiter] ~= kind, 1, 'first' ) - 1;
    idx = mod( idx_shifted + id0-1 - 1, n_arr ) + 1;
end