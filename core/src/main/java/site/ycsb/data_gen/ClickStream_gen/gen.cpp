#include<iostream>
#include<stdio.h>
#include<string.h>
#include<vector>
using namespace std;

int clk_prob[24];
int idstr = 0;
struct node
{
	int id;
	int cnt;
	vector<int> out;
	vector<int> prob;
}nd[100000];

struct vnode
{
	int uid;
	int page;
};

struct tmslot
{
	vector<vnode> vec;
}slot[24*3600];

int vnum[24*3600];

void getvnum()
{
	for(int i = 0; i < 24*3600; i++)
		vnum[i] = 1;
}

int simu(int id)
{
	vector<int> mk = nd[id].out;
	vector<int> pb = nd[id].prob;
	int cnt = nd[id].cnt;
	int i;
	int tmp = rand() % 100000;
	for (i = 0; i < cnt; i++)
	{
		if (tmp < pb[i])
			break;
	}
	return mk[i];
}

int simclk()
{
	int tmp = rand() % 100000;
	for (int i = 0; i < 24; i++)
	{
		if (tmp < clk_prob[i])
			return i;
    }
	return 23;
}

int simdelay()
{
	return 5;
}

void generate(int id, int stp, int now)
{
	
	int pg = stp;
	int tmstamp = now;
	while (1)
	{
		if(tmstamp >= 24*3600) break;
		vnode vnd;
		vnd.uid = id;
		vnd.page = pg;
		slot[tmstamp].vec.push_back(vnd);
		int next = simu(pg);
		if (next == -1)
		{
			next = simu(1);
			if (rand() % 2 == 0 || next == -1) 
				break;			
		}
		tmstamp += simdelay();
		pg = next;
	}
	return;
}

void gen()
{
	for(int i = 0; i < 24*3600; i++)
	{
		for(int j = 0; j < vnum[i]; j++)
		{
			idstr++;
			generate(idstr, 1, i);
		}
    }
	return;
}



void readfile()
{
	FILE* fp;
	fp = fopen("input", "r");
	double d;
	int num;
	
	for (int i = 0; i < 24; i++)
	{
		fscanf(fp, "%lf", &d);	
		
		if (i > 0)
			clk_prob[i] = clk_prob[i - 1] + (int)(d * 100000);
		else clk_prob[i] = (int)(d * 100000);
	}
	fscanf(fp, "%d", &num);
	for (int i = 0; i < num; i++)
	{
		int tmp, cnt;
		int sum = 0;
		fscanf(fp, "%d", &tmp);
		fscanf(fp, "%d", &cnt);
		for (int j = 0; j < cnt; j++)
		{
			int outn;
			double oprob;
			fscanf(fp, "%d", &outn);
			fscanf(fp, "%lf", &oprob);
			nd[tmp].cnt++;
			nd[tmp].out.push_back(outn);
			sum += (int)(oprob * 100000);
			nd[tmp].prob.push_back(sum);
		}
	}
	fclose(fp);
	return;
}

int main()
{
	idstr = 0;
	readfile();
	gen();
	return 0;
}
