function [C, vid_L, vid_R, vid_S, S_xT, B, eid_C] = cut_polygon(V, vid_P, cut)

n_V = size(V, 2);
n_P = size(vid_P, 2);

C = NaN(2, 0);
vid_L = {};
vid_R = {};
vid_S = zeros(1, 0);
S_xT = NaN(1, 0);
B = false(2, 0);
eid_C = zeros(1, 0);

if isempty(vid_P)
    return;
end

P = V(:, vid_P);
P_T = transform(P);
e = get_edges(P);

% determine precision used by vertex_side
eps = determine_eps;
% determine side of P's vertices
s = arrayfun(@vertex_side, P_T(2, :));

if all(s == 'l')
    vid_L = {vid_P};
    
    return;
elseif all(s == 'r')
    vid_R = {vid_P};
    
    return;
end

[C, eid_C, n_C] = calculate_crossings;
C_T = transform(C); % TODO: recalculates R-matrix

% merge polygon vertices and crossing points
PC  = [P C];
PC_T = [P_T C_T];
s  = [s repmat('b', 1, n_C)];
n_PC = size(PC, 2);

% reorder vertices
reorder_idx = calculate_vertex_order;
PC   = PC(:, reorder_idx);
PC_T = PC_T(:, reorder_idx);
s  = s(:, reorder_idx);

% determine polygon vertices on the cut
b_filt = s == 'b';
vid_b = find(b_filt);
n_b = size(vid_b, 2);

% sort cuts by 'hit order'
[~, hit_idx] = sort(PC_T(1, b_filt), 2);
vid_b = vid_b(hit_idx);
b_T = PC_T(:, vid_b);

dir_C = determine_direction;
B = cellfun(@determine_bridge_passing, ...
    mat2cell(dir_C, 2, ones(1, n_b)), ...
    num2cell(vid_b), ...
    'UniformOutput', false);
B = [B{:}];

% number of polygons after split
% N = 1 + length( strfind(B(1, :) & B(2, :), [true false]) ) ...
%     + sum( ismember(dir_C', 'll', 'rows')' & B(1, :) ) ...
%     + sum( ismember(dir_C', 'rr', 'rows')' & B(2, :) );
N = 1 + sum( B(1, :) & B(2, :) );

% build independent polygon pieces
vid_cut = build_polygons;

% determine the polygons side (left or right)
s_P = cellfun(@polygon_side, vid_cut);

% devide pieces by their side and
% restore original indexing
vid_L = cellfun(@PC2VC_vid, vid_cut(s_P == 'l'), 'UniformOutput', false);
vid_R = cellfun(@PC2VC_vid, vid_cut(s_P == 'r'), 'UniformOutput', false);

S_b_filt = determine_shared_b_vertices;
vid_S_ = vid_b(S_b_filt);
S_xT = PC_T(1, vid_S_);
B = B(:, S_b_filt);
% determine the set of vertices used by both sides
vid_S = PC2VC_vid(vid_S_);

    function eps = determine_eps
        l = sqrt( sum(diff([P P(:, 1)], 1, 2).^2) );
        eps = min(l(l > 0)) * 1e-10;
    end

    % calculate P in a coordinate system where
    % L1 is located in the origin and L2 lies on the positive x-axis
    function pts_ = transform(pts)
        n_pts = size(pts, 2);
        L1 = cut(1:2);
        L2 = cut(3:4);

        dL = L2 - L1;
        l_length = sqrt( sum(dL.^2) );

        % rotation matrix
        R = [dL(1) dL(2); -dL(2) dL(1)] / l_length;
        % transform P and l
        pts_ = R * (pts - repmat(L1, 1, n_pts));
    end

    function [pred_vid, succ_vid] = neighbors(vid)
        pred_vid = mod( vid-1 - 1, n_PC ) + 1;
        succ_vid = mod( vid+1 - 1, n_PC ) + 1;
    end

    function [C, eid_C, n_C] = calculate_crossings
        % find edge crossings with cut
        s_ = [s; s([2:end 1])]';
        eid_C = find( ismember(s_, ['lr'; 'rl'], 'rows')' );
        n_C = length(eid_C); % number of edge cuts

        % calculate crossing points
        C = NaN(2, n_C);
        for i = 1:n_C
            eid = eid_C(i);
            C(:, i) = line_line_intersect(cut, e(:, eid));
        end
    end

    function idx = calculate_vertex_order
        if n_C == 0
            idx = 1:n_P; % no change
            return;
        end
        
        idx = NaN(1, n_PC);

        last = 0;
        for i = 1:n_C
            eid_C_i = eid_C(i);
            vid_C_i = eid_C_i + i;
            I = last+1:eid_C_i;

            idx(I + i-1) = I;
            idx(vid_C_i) = n_P + i;

            last = eid_C_i;
        end

        I = eid_C(end)+1:n_P;
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

    function S_b_filt = determine_shared_b_vertices
%         B_ = [false(2, 1) B];
%         
%         tmp = B_(:, 1:end-1) | B_(:, 2:end);
%         S_b_filt = tmp(1, :) & tmp(2, :);
        
        B_ = [false B(1, :) & B(2, :)];
        S_b_filt = B_(1:end-1) | B_(2:end);
    end

    function dir_b = determine_direction
        [pred_vid, succ_vid] = neighbors(vid_b);

        dir_b = s([pred_vid; succ_vid]);
        
        if size(vid_b, 2) == 1
            dir_b = dir_b'; % stupid inconsistent matlab behaviour
        end

        filt = dir_b == 'b';
        pred_filt_idx = find(filt(1, :));
        succ_filt_idx = find(filt(2, :));

        pred_filt_vid = pred_vid(filt(1, :));
        succ_filt_vid = succ_vid(filt(2, :));

        pred_lower = PC_T(1, pred_filt_vid) < b_T(1, filt(1, :));
        succ_lower = PC_T(1, succ_filt_vid) < b_T(1, filt(2, :));

        dir_b(1, pred_filt_idx( pred_lower)) = 'u'; % up
        dir_b(1, pred_filt_idx(~pred_lower)) = 'd'; % down
        dir_b(2, succ_filt_idx( succ_lower)) = 'd'; % down
        dir_b(2, succ_filt_idx(~succ_lower)) = 'u'; % up
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

            cur_C_T  = PC_T(:, vid);
            pred_C_T = PC_T(:, pred_vid);
            succ_C_T = PC_T(:, succ_vid);

            delta1 = cur_C_T - pred_C_T;
            delta2 = succ_C_T - cur_C_T;

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
        used_vertex = false(1, n_PC);
        vid_cut = cell(1, N);
        vid_cut_i = NaN(1, n_P + N-1);
        i = 1;
        % for each polygon piece
        while ~all(used_vertex | b_filt)
            % each non-border vertex is used once
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
%                 % get last vertex of contiguous non-border vertices
%                 last = find_last_contiguous(s, cur, 'x');
                % TODO: go until next bridge

                I = interval(cur, last);

                % append interval to the vid list
                n_I = size(I, 2);
                I_ = (1:n_I) + j-1;
                vid_cut_i(I_) = I;
                used_vertex(I) = true;
                j = j + n_I;

                % determine path along the bridges

                [~, cur] = neighbors(last); % crossing point
                cid = find(vid_b == cur, 1, 'first');

                % go either up or downward the bridges
                switch side
                    case 'l'
                        upward_cid = upward(cid);
                        I_cid = cid:upward_cid;
                    case 'r'
                        downward_cid = downward(cid);
                        I_cid = cid:-1:downward_cid;
                end

                I = vid_b(I_cid);

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
                I = [first:n_PC 1:last];
            end
        end

        function upward_cid = upward(cid)
%             upward_cid = find( ~B(1, cid:end), 1, 'first' ) + cid-1;
            
            B_ = B(1, :) & B(2, :);
            upward_cid = find( ~B_(cid:end), 1, 'first' ) + cid-1;
        end

        function downward_cid = downward(cid)
%             downward_cid = find( [true ~B(2, 1:cid-1)], 1, 'last' );
            
            B_ = B(1, :) & B(2, :);
            downward_cid = find( [true ~B_(1:cid-1)], 1, 'last' );
        end

        function next_vid = next(vid)
            if vid == n_PC
                next_vid = 1;
            else
                next_vid = vid+1;
            end
        end

        function prev_vid = prev(vid)
            if vid == 1
                prev_vid = n_PC;
            else
                prev_vid = vid-1;
            end
        end
    end

    function vid_ = PC2VC_vid(vid)
        % restore order from before reordering
        vid_ = reorder_idx(vid);
        
        % restore original vid_P
        P_filt = vid_ <= n_P;
        vid_(P_filt) = vid_P( vid_(P_filt) );
        
        % adjust new crossing vertices' indices
        C_filt = not(P_filt);
        vid_(C_filt) = vid_(C_filt) + n_V - n_P;
    end
end

function idx = find_last_contiguous(arr, id0, delimiter)
    n_arr = size(arr, 2);
    shifted = circshift(arr, [0 1-id0]);
    kind = arr(id0);

    idx_shifted = find( [shifted delimiter] ~= kind, 1, 'first' ) - 1;
    idx = mod( idx_shifted + id0-1 - 1, n_arr ) + 1;
end

function idx = until(b, id0)
    n_b = length(b);
    b_ = circshift(b, [0 -(id0-1)]);
    idx_ = find(b_, 1, 'first');
    
    if isempty(idx_)
        idx_ = n_b;
    end
    
    idx = idxmod(idx_ + id0-1 - 1, n_b);
end