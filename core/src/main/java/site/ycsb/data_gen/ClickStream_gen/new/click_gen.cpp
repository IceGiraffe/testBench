#include<iostream>
#include<fstream>
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
}slot[24 * 3600];

int vnum[24 * 3600];  //存储每一秒的新进用户数量

void getvnum()
{
	for (int i = 0; i < 24 * 3600; i++)
		vnum[i] = 1;
}

//生成下一轮访问的页号
int simu(int id)
{
	vector<int> mk = nd[id].out;
	vector<int> pb = nd[id].prob;
	int cnt = nd[id].cnt;
	int i;
	int tmp = rand() % 10000;
	for (i = 0; i < cnt; i++)
	{
		if (tmp < pb[i])
			break;	
	}
	return mk[i];
}

int simclk()
{
	int tmp = rand() % 10000;
	for (int i = 0; i < 24; i++)
	{
		if (tmp < clk_prob[i])
			return i;
	}
	return 23;
}

//访问下一个网页的时间间隔
int simdelay()
{
	return 5;
}

//一次性生成用户id的访问序列，根据时间将各个步骤加入时间槽
void generate(int id, int stp, int now)
{

	int pg = stp;
	int tmstamp = now;
	while (1)
	{
		if (tmstamp >= 24 * 3600) break;
		vnode vnd;
		vnd.uid = id;
		vnd.page = pg;
		slot[tmstamp].vec.push_back(vnd);
		int next = simu(pg);
		if (next == -1)
		{
			next = simu(0);
			if (rand() % 2 == 0 || next == -1)
				break;
		}
		tmstamp += simdelay();
		pg = next;
	}
	return;
}

//按时间序列进行生成，每秒需要打印新进的访问行为和已经生成过但刚刚发生的访问
void gen()
{
	FILE* fw;
	fw = fopen("output.txt", "w");
	int st = 0;
	for (int i = 0; i < 24 * 3600; i++)
	{
		for (int j = 0; j < vnum[i]; j++)
		{
			idstr++;
			generate(idstr, st, i);
		}
		vector<vnode> vc = slot[i].vec;
		for (int tt = 0; tt < vc.size(); tt++)
		{
			vnode vnd = vc[tt];
			fprintf(fw, "user %d visit page %d at %d sec.\n", vnd.uid, vnd.page, i);
		}
	}
	fclose(fw);
	return;
}



void readfile()
{
	FILE* fp;
	fp = fopen("input.txt", "r");
	double d;
	int num;
	for (int i = 0; i < 24; i++)
	{
		fscanf(fp, "%lf", &d);
		if (i > 0)
			clk_prob[i] = clk_prob[i - 1] + (int)(d * 10000);
		else clk_prob[i] = (int)(d * 10000);
	}
	fscanf(fp, "%d", &num);
	for (int i = 0; i < num; i++)
	{
		int tmp, cnt;
		int sum = 0;
		fscanf(fp, "%d", &tmp);
		fscanf(fp, "%d", &cnt);
		nd[tmp].cnt = cnt;
		for (int j = 0; j < cnt; j++)
		{
			int outn;
			double oprob;
			fscanf(fp, "%d", &outn);
			fscanf(fp, "%lf", &oprob);
			nd[tmp].out.push_back(outn);
			sum += (int)(oprob * 10000);
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
	getvnum();
	gen();
	return 0;
}