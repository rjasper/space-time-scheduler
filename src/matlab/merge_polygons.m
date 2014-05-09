function [V, vid, R2V_map] = merge_polygons(V_L, vid_L, vid_S_L, V_R, vid_R, vid_S_R)

n_L = size(V_L, 2);
n_R = size(V_R, 2);
n_S = length(vid_S_L); % == length(vid_S_R)

if n_S == 0 % == isempty(vid_S_R)
    V = [V_L V_R];
    R2V_map = n_L + 1:n_R;
    vid = [vid_L cellfun(@(v) n_L + v, vid_R, 'UniformOutput', false)];
    
    return;
end

N_L = length(vid_L);
N_R = length(vid_R);
N_P_max = N_L + N_R;
n_P_max = sum([ cellfun(@length, vid_L) cellfun(@length, vid_R) ]);
n_B = n_S - 1;

% calculate index mapping from V_R to V
non_vid_S_R = setdiff(1:n_R, vid_S_R);
R2V_map = zeros(1, n_R);
R2V_map(vid_S_R) = vid_S_L;
R2V_map(non_vid_S_R) = n_L + (1:n_R-n_S);

V = [V_L V_R(:, non_vid_S_R)];

vid = cell(1, N_P_max);

[mapTo_L, mapFrom_L] = determine_mapping(vid_L, vid_S_L, 'left');
[mapTo_R, mapFrom_R] = determine_mapping(vid_R, vid_S_R, 'right');

% merge polygons connected by bridges

unused_B = mapFrom_L(1, :) ~= 0 & mapFrom_R(1, :) ~= 0;
unused_L = true(1, N_L);
unused_R = true(1, N_R);
vid_i = zeros(1, n_P_max);
i = 1;
while any(unused_B)
    % find next unused bridge
    bid_first = find(unused_B, 1, 'first');
    bid = bid_first;

    no_loop = true;

    j = 1;
    while no_loop
        unused_B(bid) = false;

        pid_R = mapFrom_R(1, bid);
        pvid_start_R = mapFrom_R(2, bid);
        vid_R_pid = vid_R{pid_R};
        n_R_pid = length(vid_R_pid);
        isB_R = mapTo_R{pid_R} ~= 0;

        unused_R(pid_R) = false;

        % go to next bridge start (might be cur)
        pvid_Bstart_R = bnext(isB_R, pvid_start_R);
        pvid_end_R = idxmod(pvid_Bstart_R-1, n_R_pid);

        I_R = interval(pvid_start_R, pvid_end_R, n_R_pid);
        n_I_R = length(I_R);
        vid_i(j-1 + (1:n_I_R)) = R2V_map( vid_R_pid(I_R) );
        j = j + n_I_R;

        % cross bridge to L

        bid = mapTo_R{pid_R}(pvid_Bstart_R);
        unused_B(bid) = false;

        pid_L = mapFrom_L(1, bid);
        pvid_start_L = mapFrom_L(2, bid);
        vid_L_pid = vid_L{pid_L};
        n_L_pid = length(vid_L_pid);
        isB_L = mapTo_L{pid_L} ~= 0;

        unused_L(pid_L) = false;

        % go to next bridge start
        pvid_Bstart_L = bnext(isB_L, pvid_start_L);
        pvid_end_L = idxmod(pvid_Bstart_L-1, n_L_pid);

        I_L = interval(pvid_start_L, pvid_end_L, n_L_pid);
        n_I_L = length(I_L);
        vid_i(j-1 + (1:n_I_L)) = vid_L_pid( I_L );
        j = j + n_I_L;

        % cross bridge back to R
        bid = mapTo_L{pid_L}(pvid_Bstart_L);

        % check if polygon is complete
        no_loop = bid ~= bid_first;
    end

    vid{i} = vid_i(1:j-1);
    
    i = i + 1;
end

% translate indices of remaining polygons (non-cut)

vid_L_ = vid_L(unused_L);
vid_R_ = cellfun(@(v) R2V_map(v), vid_R(unused_R), 'UniformOutput', false);

% collect all polygons (merged and non-cut polygons)
vid = [vid(1:i-1) vid_L_ vid_R_];

    % determines the from-vertex-to-bridge (mapTo) and
    % from-bridge-to-vertex (mapFrom) mapping
    function [mapTo, mapFrom] = determine_mapping(vid, vid_S, side)
        % number of polygons
        N_P = length(vid);
        
        mapFrom = zeros(2, n_B);
        % note that mapFrom will be modified by the helper's side effect
        % however, the modifications will be independent from each other
        mapTo = cellfun(@helper, vid, num2cell(1:N_P), 'UniformOutput', false);
        
        % changes mapFrom
        function mapTo = helper(vid, i)
            % number of polygon vertices
            n_P = length(vid);
            
            % find vertices on the cut
            [isS, S_idx] = ismember(vid, vid_S);
            % find bridges
            B_filt = isS & isS([2:end 1]);
            B_idx = find(B_filt);
            B_idx_ = idxmod(B_idx+1, n_P);

            % determine bridge IDs
            switch side
                case 'left'
                    B = S_idx( B_filt );
                case 'right'
                    S_idx_ = circshift(S_idx, [0 -1]);
                    B = S_idx_( B_filt );
            end
            
            mapTo = zeros(1, n_P);
            mapTo(B_filt) = B;
            
            mapFrom(1, B) = i; % polygon ID (pid)
            mapFrom(2, B) = B_idx_; % polygon vertex ID (pvid)
        end
    end
end

% gets the next true index after the given one
% is cyclic
function next = bnext(b, idx)
    b_shift = circshift(b, [0 1-idx]);

    next_shift = find(b_shift, 1, 'first');
    next = idxmod( next_shift + idx-1, length(b) );
end

% gives the interval between i_start and i_end
% is cyclic
function I = interval(i_start, i_end, n)
    if i_start <= i_end
        I = i_start:i_end;
    else
        I = [i_start:n 1:i_end];
    end
end