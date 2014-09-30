%% data

v_max = 2;
I_st = [0; 0];
s_max = 10;
t_max = 10;
t_spare = 2.1;

Om_st = {
    [8 7; 12 7; 12 8; 8 8]'
};

%% calculation

[A_st, V_st, idx_F, pid] = minimum_time_vgraph(I_st, Om_st, s_max, t_max, v_max, t_spare);

%% plot

f1 = figure(1);
clf reset;
% draw_polygon(Om_st, 'g');
hold on;
draw_graph(V_st, A_st);
hold off;
