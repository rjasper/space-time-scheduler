function [C, vid_L, vid_R, vid_S, S_xT, B, eid_C] = cut_polygon(V, vid_P, cut)

n_V = size(V, 2);
n_P = size(vid_P, 2);

C = NaN(2, 0);
vid_L = {};
vid_R = {};
vid_S = zeros(1, 0);
S_xT = NaN(1, 0);
B = false(1, 0);
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

if ~any(s == 'r')
    vid_L = {vid_P};
    
    return;
elseif ~any(s == 'l')
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
filt_b = s == 'b';
vid_b = find(filt_b);
n_b = size(vid_b, 2);

% sort cuts by 'hit order'
[~, hit_idx] = sort(PC_T(1, filt_b), 2);
vid_b = vid_b(hit_idx);
b_T = PC_T(:, vid_b);

% determine the direction of the edges of the border vertices
dir_C = determine_direction;

% determine bridges between border vertices
B_b_ = cellfun(@determine_bridge_passing, ...
    mat2cell(dir_C, 2, ones(1, n_b)), ...
    num2cell(vid_b), ...
    'UniformOutput', false);
B_b = [B_b_{:}];

% number of polygons after split
N = 1 + sum( B_b );

filt_S_b = determine_shared_vertices;
vid_S_ = vid_b(filt_S_b);
n_S = length(vid_S_);
% bridges between shared vertices
% note that there is one unset dummy bridge at the end for convenience
B = B_b(filt_S_b);

% build independent polygon pieces
vid_cut = build_polygons;

% determine the polygons side (left or right)
s_P = cellfun(@polygon_side, vid_cut);

% devide pieces by their side and
% restore original indexing
vid_L = cellfun(@PC2VC_vid, vid_cut(s_P == 'l'), 'UniformOutput', false);
vid_R = cellfun(@PC2VC_vid, vid_cut(s_P == 'r'), 'UniformOutput', false);

S_xT = PC_T(1, vid_S_);
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

    function vid_next = succ_vid(vid)
        vid_next = idxmod(vid+1, n_PC);
    end

    function vid_pred = pred_vid(vid)
        vid_pred = idxmod(vid-1, n_PC);
    end

    function [vid_pred, vid_succ] = neighbors(vid)
        vid_pred = pred_vid(vid);
        vid_succ = succ_vid(vid);
    end

    function filt_S_b = determine_shared_vertices
        % the border vertex must be either the entrance or the exit of
        % a bridge
        filt_S_b = [false B_b(1:end-1)] | B_b;
    end

    function dir_b = determine_direction
        [pred_vid, succ_vid] = neighbors(vid_b);

        dir_b = s([pred_vid; succ_vid]);
        
        if size(vid_b, 2) == 1
            dir_b = dir_b'; % stupid inconsistent matlab behaviour
        end

        filt_b_dir = dir_b == 'b';
        pred_filt_idx = find(filt_b_dir(1, :));
        succ_filt_idx = find(filt_b_dir(2, :));

        pred_filt_vid = pred_vid(filt_b_dir(1, :));
        succ_filt_vid = succ_vid(filt_b_dir(2, :));

        pred_lower = PC_T(1, pred_filt_vid) < b_T(1, filt_b_dir(1, :));
        succ_lower = PC_T(1, succ_filt_vid) < b_T(1, filt_b_dir(2, :));

        dir_b(1, pred_filt_idx( pred_lower)) = 'u'; % up
        dir_b(1, pred_filt_idx(~pred_lower)) = 'd'; % down
        dir_b(2, succ_filt_idx( succ_lower)) = 'd'; % down
        dir_b(2, succ_filt_idx(~succ_lower)) = 'u'; % up
    end

    function B_b = determine_bridge_passing(dir, vid)
        pred = dir(1);
        succ = dir(2);
        
        if (pred == 'u' || pred == 'l') && (succ == 'd' || succ == 'r')
            B_b = true;
        elseif (pred == 'l' && succ == 'l') || (pred == 'r' && succ == 'r')
            [pred_vid, succ_vid] = neighbors(vid);

            cur_C_T  = PC_T(:, vid);
            pred_C_T = PC_T(:, pred_vid);
            succ_C_T = PC_T(:, succ_vid);

            delta1 = cur_C_T - pred_C_T;
            delta2 = succ_C_T - cur_C_T;

            m1 = -delta1(1)/delta1(2); % -cot(alpha1)
            m2 = -delta2(1)/delta2(2); % -cot(alpha2)

            B_b = m1 <= m2;
        else
            B_b = false;
        end
    end

    function vid_cut = build_polygons
        used_vertex = false(1, n_PC);
        vid_cut = cell(1, N);
        vid_cut_i = zeros(1, n_P + N-1);
        
        filt_S = ind2filt(vid_S_, n_PC);
        % indicates the first border vertex of each contiguous series
        hr_S = rising_edge(filt_S, 'high', 'circular');
        % indicates the first non-border vertex of each contiguous series
        lf_S = falling_edge(filt_S, 'low', 'circular');
        
        i = 1;
        % for each polygon piece
        while ~all(used_vertex | filt_S)
            % each non-border vertex is used once

            % get first vertex after a border vertex
            vid_first = find(~used_vertex & lf_S, 1, 'first');

            cur = vid_first; % the first vertex of the polygon
            side = s(cur); % the side of the polygon (left or right)
            no_loop = true; % indicates whether the polygon is complete
            j = 1; % current vertex' index

            % for each contiguous range of non-border vertices of the current
            % polygon piece
            while no_loop
                % find the next border vertex
                last = next(hr_S, cur, 'circular');

                I = intervalc(cur, last);

                % append interval to the vid list
                n_I = size(I, 2);
                I_ = (1:n_I) + j-1;
                vid_cut_i(I_) = I;
                used_vertex(I) = true;
                j = j + n_I;

                % determine path along the bridges

                cid = find(vid_S_ == last, 1, 'first');

                % go either up or downward the bridges
                switch side
                    case 'l'
                        upward_cid = upward(cid);
                        I_cid = cid+1:upward_cid;
                    case 'r'
                        downward_cid = downward(cid);
                        I_cid = cid-1:-1:downward_cid;
                end

                I = vid_S_(I_cid);

                % append interval to the vid list
                n_I = size(I, 2);
                I_ = (1:n_I) + j-1;
                vid_cut_i(I_) = I;
                j = j + n_I;

                cur = succ_vid(I(end));
                % detect loop
                no_loop = cur ~= vid_first;
            end

            % store polygon piece
            vid_cut{i} = vid_cut_i(:, 1:j-1);
            i = i + 1;
        end

        function I = intervalc(first, last)
            if first <= last
                I = first:last;
            else
                I = [first:n_PC 1:last];
            end
        end

        function upward_cid = upward(cid)
            % side of the entrance vertex
            s_S_next = s(succ_vid(vid_S_(1:end-1)));
            % indicates the current border vertex
            filt_cid = ind2filt(cid, n_S-1);
            
            % an upward bridge is passable if the entrance vertex
            % is either the starting point (cid) or is not on the left
            passable = B(1:end-1) & (s_S_next ~= 'l' | filt_cid);
            
            % go until the last passable bridge exit vertex
            upward_cid = next(falling_edge([passable, false], 'low'), cid);
        end

        function downward_cid = downward(cid)
            % side of the entrance vertex
            s_S_next = s(succ_vid(vid_S_(2:end)));
            % indicates the current border vertex
            filt_cid = ind2filt(cid-1, n_S-1);
            
            % a downward bridge is passable if the entrance vertex
            % is either the starting point (cid) or is not on the right
            passable = B(1:end-1) & (s_S_next ~= 'r' | filt_cid);
            
            % go until the last passable bridge exit vertex
            downward_cid = prev(rising_edge([false, passable], 'low'), cid);
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