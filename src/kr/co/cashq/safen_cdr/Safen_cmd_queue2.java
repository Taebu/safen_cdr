package kr.co.cashq.safen_cdr;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//import com.nostech.safen.SafeNo;

/**

 * safen_cmd_queue 테이블 관련 객체
 * @author mtb
 * 2017-03-10 (금) 13:36:49 
 *  리뷰 포인트 90일 60일로 변경
 *  
 *  2017-08-18 (금) 16:10:00
 *   세일 포인트 추가
 *   동시 적립 가능하도록 변경 
 *   
 */
public class Safen_cmd_queue2 {
	
	/**
	 * safen_cmd_queue 테이블의 데이터를 처리하기 위한 주요한 처리를 수행한다.
	 */
	public static void doMainProcess() {
		Connection con = DBConn.getConnection();

		String ev_st_dt="";
		String ev_ed_dt="";
		String daily_st_dt="";
		String daily_ed_dt="";
		String review_ed_dt="";
		String mb_hp="";
		String eventcode="";
		String ed_type="";
		String biz_code="";
		String call_hangup_dt="";
		String mb_id="";
		String certi_code="";
		String st_dt="";
		String ed_dt="";
		String moddate="1970-01-01 12:00:00";
		String accdate="1970-01-01 12:00:00";
		String str_hangup_time="";
		String tel="";
		String pt_stat="";

		int eventcnt = 0;
		int daycnt = 0;
		int reviewdaycnt = 0;
		int downdaycnt = 0;
		int saledaycnt = 0;
		int calldaycnt = 0;
		int ciddaycnt = 0;
		
		int pt_day_cnt = 0;
		int pt_event_cnt = 0;
		int usereventindex = 0;
		int user_event_dt_index = 0;
		int tcl_seq = 0;
		int service_sec=0;
		int hangup_time=0;

		boolean is_hp = false;
		boolean is_freedailypt = false;
		boolean is_freeuserpt = false;
		boolean is_fivept = false;
		boolean is_callpt = false;
		boolean is_reviewpt = false;
		boolean is_downpt = false;
		boolean is_salept = false;
		
		boolean is_realcode=false;
		boolean is_userpt=false;
		boolean chk_realcode=false;
		boolean is_answer=false;
		boolean is_biz_code = false;
		boolean is_cidpoint_condition = false;
		/* 상점 정보 */
		String[] store_info			= new String[5];
		/* 포인트 이벤트 정보 */
		Map<String, String> point_event_info = new HashMap<String, String>();
		/* 유저 이벤트 정보 */
		String[] user_event_info	= new String[3];
		
		Map<String, String> cidpoint_info = new HashMap<String, String>();
		
		Map<String, String> call_point_info = new HashMap<String, String>();
		String status_cd="";
		String conn_sdt="";
		String conn_edt="";
		String service_sdt="";
		String safen="";
		String safen_in="";
		String safen_out="";
		String calllog_rec_file="";

		String store_name = "";
		String pre_pay="";
		String store_seq="";
		String type="";
		String str_tcl_seq="";
		String cp_no = "";
		
		if (con != null) {
			MyDataObject dao = new MyDataObject();
			MyDataObject dao2 = new MyDataObject();
			MyDataObject dao3 = new MyDataObject();
			MyDataObject dao4 = new MyDataObject();
			MyDataObject dao5 = new MyDataObject();

			StringBuilder sb = new StringBuilder();
			StringBuilder sb_log = new StringBuilder();

			sb.append("select * from prq.prq_cidpoint_log order by cp_no;");
			
			try {
				dao.openPstmt(sb.toString());
				dao.setRs(dao.pstmt().executeQuery());

				while(dao.rs().next()) 
				{
					SAFEN_CDR.heart_beat = 1;
					Boolean is_cp_no=dao.rs().getInt("cp_no")>0;

					if (is_cp_no) 
					{
						
						String hist_table = DBConn.isExistTableYYYYMM("prq", "prq_cidpoint_log");

						/* resultSet 을 Map<string,String>형태로 변환 한다. */
						cidpoint_info = getResultMapRows(dao.rs());
						
						biz_code=cidpoint_info.get("prq_store.biz_code");
						cp_no=cidpoint_info.get("cp_no");
						point_event_info=getEventCodeInfo(biz_code);
						
						is_biz_code = biz_code!=null||biz_code!="";
						/* 4-3. Event Info 조회*/
						if(is_biz_code&&point_event_info.size()>2)
						{
							pre_pay = "sl";
							mb_hp = cidpoint_info.get("mb_hp");
							call_point_info.put("mb_hp", mb_hp);
							call_point_info.put("point", cidpoint_info.get("prq_store.cid_point"));
							call_point_info.put("store_name", cidpoint_info.get("prq_store.st_name"));
							call_hangup_dt = add_second(cidpoint_info.get("cp_datetime"),5);
							st_dt = cidpoint_info.get("cp_datetime");
							ed_dt = add_second(cidpoint_info.get("cp_datetime"),19);
							call_point_info.put("call_hangup_dt", call_hangup_dt);
							call_point_info.put("biz_code", biz_code);
							
							ev_st_dt=point_event_info.get("ev_st_dt");
							ev_ed_dt=point_event_info.get("ev_ed_dt");
							eventcode=point_event_info.get("eventcode");
							
							call_point_info.put("ev_st_dt", ev_st_dt);
							call_point_info.put("ev_ed_dt", ev_ed_dt);
							call_point_info.put("eventcode", eventcode);
							
							call_point_info.put("mb_id", "");
							call_point_info.put("certi_code", "");
							
							call_point_info.put("st_dt", st_dt);
							call_point_info.put("ed_dt", ed_dt);
							call_point_info.put("tcl_seq", "0");
							call_point_info.put("store_seq", "0");
							call_point_info.put("status", "1");
							call_point_info.put("moddate", "0000-00-00 00:00:00");
							call_point_info.put("accdate", "0000-00-00 00:00:00");
							call_point_info.put("cashq_seq", "0");
							call_point_info.put("memo", "");
							tel = cidpoint_info.get("prq_store.mb_id");
							call_point_info.put("tel", tel);
							call_point_info.put("cnt_memo", "");
							call_point_info.put("pre_pay", "sl");
							call_point_info.put("pt_stat", "pt5");
							ed_type=point_event_info.get("ed_type");
							call_point_info.put("ed_type", ed_type);
	
							service_sec = 15;
							pt_day_cnt = Integer.parseInt(point_event_info.get("pt_day_cnt"));
							pt_event_cnt = Integer.parseInt(point_event_info.get("pt_event_cnt"));
							ed_type=point_event_info.get("ed_type");
							
							if(eventcode.length()>3&&biz_code.length()>3){
								chk_realcode=is_realcode(eventcode,biz_code);
							}

							is_hp = is_hp(mb_hp);
							ciddaycnt =  get_checkpoint("cidpt",mb_hp);
						
						}/* if(biz_code!=null||biz_code!=""){ ... } */

						/* 4-4 */
						eventcnt = get_eventcnt(mb_hp,eventcode,ed_type);
						/* 4-13 */
						pt_stat=chk_pt5(ed_type);

						
						is_cidpoint_condition = is_point(pre_pay);
						is_cidpoint_condition = is_cidpoint_condition&&service_sec>9;
						is_cidpoint_condition = is_cidpoint_condition&&is_datepoint(ev_st_dt,ev_ed_dt);
						is_cidpoint_condition = is_cidpoint_condition&&ciddaycnt==0;
						is_cidpoint_condition = is_cidpoint_condition&&eventcnt<pt_event_cnt;
						is_cidpoint_condition = is_cidpoint_condition&&is_hp;
						is_cidpoint_condition = is_cidpoint_condition&&chk_realcode; 
								
						/*6. 적립조건
						 * is_cidpoint_condition
						 * */
						if(is_cidpoint_condition){
							set_0507_point(call_point_info);
							set_checkpoint("cidpt",mb_hp);
						}

						done_cidpoint_log(hist_table, cp_no);
					}
				}				
			} catch (SQLException e) {
				Utils.getLogger().warning(e.getMessage());
				DBConn.latest_warning = "ErrPOS031";
				e.printStackTrace();
			} catch (Exception e) {
				Utils.getLogger().warning(e.getMessage());
				Utils.getLogger().warning(Utils.stack(e));
				DBConn.latest_warning = "ErrPOS032";
			}finally {
				dao.closePstmt();
				dao2.closePstmt();
				dao3.closePstmt();
				dao4.closePstmt();
				dao5.closePstmt();
			}
			
			//콜로그 마스터 정보의 레코드 1개를 갱신을 시도한다.
			//Safen_master.doWark2();
		}
	}


	/**
	 * 0507_point에 추가한다.
     * @param  Map<String, String> point_info
	 * @return void
	 */
	public static void set_0507_point(Map<String, String> point_info) 
	{

		StringBuilder sb = new StringBuilder();
		MyDataObject dao = new MyDataObject();
		sb.append("INSERT INTO `cashq`.`0507_point` SET ");
		
		sb.append("mb_hp=?,"); /* 1 */
		sb.append("point=?,"); /* 2 */
		sb.append("store_name=?,"); /* 3 */
		sb.append("type=?,"); /* 4 */
		sb.append("hangup_time=?,"); /* 5 */

		sb.append("call_hangup_dt=?,"); /* 6 */
		sb.append("biz_code=?,"); /* 7 */
		sb.append("ev_st_dt=?,"); /* 8 */
		sb.append("ev_ed_dt=?,"); /* 9 */
		sb.append("eventcode=?,"); /* 10 */
		
		sb.append("mb_id=?,"); /* 11 */
		sb.append("certi_code=?,"); /* 12 */
		sb.append("insdate=now(),");
		sb.append("st_dt=?,"); /* 13 */
		sb.append("ed_dt=?,"); /* 14 */
		
		sb.append("tcl_seq=?,"); /* 15*/
		sb.append("store_seq=?,"); /* 16*/
		sb.append("status='1',");
		sb.append("moddate='0000-00-00 00:00:00',");
		sb.append("accdate='0000-00-00 00:00:00',");
		
		sb.append("cashq_seq='0',"); 
		sb.append("memo='',");
		sb.append("tel=?,"); /* 17*/
		sb.append("cnt_memo='',");
		sb.append("pre_pay='sl',");
		
		sb.append("pt_stat='pt5',");
		sb.append("ed_type='cidpt';");
		
		try {
			dao.openPstmt(sb.toString());

			dao.pstmt().setString(1,  point_info.get("mb_hp"));
			dao.pstmt().setString(2,  point_info.get("point"));
			dao.pstmt().setString(3,  point_info.get("store_name"));
			dao.pstmt().setString(4,  "W01");
			dao.pstmt().setString(5,  "14");
			dao.pstmt().setString(6,  point_info.get("call_hangup_dt"));
			dao.pstmt().setString(7,  point_info.get("biz_code"));
			dao.pstmt().setString(8,  point_info.get("ev_st_dt"));
			dao.pstmt().setString(9,  point_info.get("ev_ed_dt"));
			dao.pstmt().setString(10,  point_info.get("eventcode"));
			dao.pstmt().setString(11, point_info.get("mb_id"));
			dao.pstmt().setString(12, point_info.get("certi_code"));
			dao.pstmt().setString(13, point_info.get("st_dt"));
			dao.pstmt().setString(14, point_info.get("ed_dt"));
			dao.pstmt().setString(15, point_info.get("tcl_seq"));
			dao.pstmt().setString(16, point_info.get("store_seq"));
			dao.pstmt().setString(17, point_info.get("tel"));

			dao.pstmt().executeUpdate();
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS060";
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS061";
		} finally {
			dao.closePstmt();
		}
	}



	
	/**
	 * cdr 에 추가한다.
	 * @param String status_cd	콜로그 상태 코드
	 * @param String conn_sdt	콜로그 시작시간
	 * @param String conn_edt	콜로그 종료시간
	 * @param String service_sdt	콜로그 제공시간
	 * @param String safen	
	 * @param String safen_in
	 * @param String safen_out
	 * @param String calllog_rec_file
	 * @return
	 */
	public static int set_cdr(String status_cd, 
		String conn_sdt, String conn_edt,String service_sdt,
		String safen,String safen_in,String safen_out,
		String calllog_rec_file) 
	{
		boolean retVal = false;
		int last_id = 0;
		StringBuilder sb = new StringBuilder();
		MyDataObject dao = new MyDataObject();
		sb.append("INSERT INTO `asteriskcdrdb`.`cdr` SET ");
		sb.append("calldate=?,");
		sb.append("src=?,");
		sb.append("dst=?,");
		sb.append("duration=?,");
		sb.append("billsec=?,");
		sb.append("accountcode=?,");
		sb.append("uniqueid=?,");
		sb.append("userfield=?;");
		//Utils.getLogger().warning(sb.toString());


		/*
		sb.append("insert into cashq.site_push_log set "
				+ "stype='SMS', biz_code='ANP', caller=?, called=?, wr_subject=?, regdate=now(), result=''");
		*/
		try {
			dao.openPstmt(sb.toString());

			dao.pstmt().setString(1, dao.rs().getString("conn_sdt"));
			dao.pstmt().setString(2, dao.rs().getString("safen_in"));
			dao.pstmt().setString(3, dao.rs().getString("safen"));
			dao.pstmt().setString(4, dao.rs().getString("conn_sec"));
			dao.pstmt().setString(5, dao.rs().getString("service_sec"));
			dao.pstmt().setString(6, dao.rs().getString("safen_out"));
			dao.pstmt().setString(7, dao.rs().getString("unique_id"));
			dao.pstmt().setString(8, dao.rs().getString("rec_file_cd"));

			dao.pstmt().executeUpdate();
			retVal = true;
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS060";
			/* grant로 해당 사용자에 대한 권한을 주어 문제 해결이 가능하다.
			grant all privileges on cashq.site_push_log to sktl@"%" identified by 'sktl@9495';
			grant all privileges on cashq.site_push_log to sktl@"localhost" identified by 'sktl@9495';
			 */
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS061";
		} finally {
			dao.closePstmt();
		}
		//return retVal;
		return last_id;
	}
	
	
	/**
	 * 비즈코드에 따른 이벤트 코드 정보를 리턴한다.
	 * @param biz_code
	 * @return
	 */
	private static Map<String,String> getEventCodeInfo(String biz_code) {
		
		StringBuilder sb = new StringBuilder();
		MyDataObject dao = new MyDataObject();
		
		Map<String, String> point_event_dt=new HashMap<String, String>();

		sb.append("SELECT * FROM `cashq`.`point_event_dt` ");
		sb.append("WHERE biz_code=? and used='1' ");
		sb.append("order by seq desc limit 1;");

		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, biz_code);
			
			dao.setRs (dao.pstmt().executeQuery());

			if (dao.rs().next()) {
				point_event_dt = getResultMapRows(dao.rs());
			}			
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS039";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS040";
		}
		finally {
			dao.closePstmt();
		}

		return point_event_dt;
	}
	
	/**
	* is_realcode
	*/
	private static boolean is_realcode(String eventcode,String biz_code) {
		boolean is_code=false;

		String[] explode=eventcode.split("\\_");

		is_code=explode[0].equals(biz_code);
		return is_code;
	}

	/**
	* int get_eventcnt
	* @param mb_hp
	* @param eventcode
	* @return int
	*/
	private static int get_eventcnt(String mb_hp, String eventcode,String ed_type){
		int retVal = 0;
		StringBuilder sb = new StringBuilder();

		MyDataObject dao = new MyDataObject();
		sb.append("SELECT count(*) cnt FROM `cashq`.`0507_point` ");
		sb.append("WHERE mb_hp=? ");
		sb.append("AND eventcode=? ");
		sb.append("AND ed_type=? ");
		sb.append("AND status in ('1','2','3','4');");

		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, mb_hp);
			dao.pstmt().setString(2, eventcode);
			dao.pstmt().setString(3, ed_type);
			
			dao.setRs (dao.pstmt().executeQuery());

			if (dao.rs().next()) {
				retVal = dao.rs().getInt("cnt");
			}			
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS039";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS040";
		}
		finally {
			dao.closePstmt();
		}

		return retVal;
	}
	/**
	* int get_daycnt
	* @param mb_hp
	* @return int
	*/
	private static int get_daycnt(String mb_hp,String ed_type){
		int retVal = 0;
		StringBuilder sb = new StringBuilder();

		MyDataObject dao = new MyDataObject();
		sb.append("SELECT count(*) cnt FROM `cashq`.`0507_point` ");
		sb.append("WHERE mb_hp=? ");
		sb.append("AND date(st_dt)=date(now()) ");
		sb.append("AND status in ('1','2','3','4') ");
		if(ed_type.equals("callpt")){
			sb.append(" AND ed_type not in ('downpt','reviewpt') ");
		}else if(ed_type.equals("reviewpt")){
			sb.append("  AND ed_type='reviewpt' ");
		}else if(ed_type.equals("downpt")){
			sb.append(" AND ed_type='downpt' ");
		} 
		
		try {
			dao.openPstmt(sb.toString());
			
			//Utils.getLogger().warning(sb.toString());
			
			dao.pstmt().setString(1, mb_hp);
			
			
			dao.setRs (dao.pstmt().executeQuery());

			if (dao.rs().next()) {
				retVal = dao.rs().getInt("cnt");
			}			
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS039";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS040";
		}
		finally {
			dao.closePstmt();
		}

		return retVal;
	}

	/**
	* boolean is_hp
	* @param hp
	* @return boolean
	*/
	private static boolean is_hp(String hp){
		boolean retVal=false;
			if(hp.length()>=2){
				retVal=hp.substring(0,2).equals("01");
			}
		return retVal;
	}
	
	/**
	* boolean is_freedailypt
	* @param ed_type
	* @return boolean
	*/
	private static boolean is_freedailypt(String ed_type){
		boolean retVal=false;
		if(ed_type!=null){
			if(ed_type.length()>=11){
				retVal = ed_type.substring(0,11).equals("freedailypt");
			}
		}
		return retVal;
	}


	/**
	* boolean is_freeuserpt
	* @param ed_type
	* @return boolean
	*/
	private static boolean is_freeuserpt(String ed_type){
		boolean retVal=false;
		if(ed_type!=null){
			if(ed_type.length()>=10){
				retVal = ed_type.substring(0,10).equals("freeuserpt");
			}
		}
		return retVal;
	}

	/**
	* boolean is_fivept
	* @param ed_type
	* @return boolean
	*/
	private static boolean is_fivept(String ed_type){
		boolean retVal=false;
		if(ed_type!=null){
			if(ed_type.length()>=6){
				retVal = ed_type.substring(0,6).equals("fivept");
			}else{
				retVal = ed_type.equals("");
			}
		}
		return retVal;
	}
	/**
	* boolean is_fivept
	* @param ed_type
	* @return boolean
	*/
	private static boolean is_downpt(String ed_type){
		boolean retVal=false;
		if(ed_type!=null){
			if(ed_type.length()>=6){
				retVal = ed_type.substring(0,6).equals("downpt");
			}else{
				retVal = ed_type.equals("");
			}
		}
		return retVal;
	}
	/**
	* boolean is_fivept
	* @param ed_type
	* @return boolean
	*/
	private static boolean is_reviewpt(String ed_type){
		boolean retVal=false;
		if(ed_type!=null){
			if(ed_type.length()>=8){
				retVal = ed_type.substring(0,8).equals("reviewpt");
			}else{
				retVal = ed_type.equals("");
			}
		}
		return retVal;
	}

	/**
	* boolean is_salept
	* @param ed_type
	* @return boolean
	*/
	private static boolean is_salept(String ed_type){
		boolean retVal=false;
		if(ed_type!=null){
			if(ed_type.length()>=5){
				retVal = ed_type.substring(0,6).equals("salept");
			}else{
				retVal = ed_type.equals("");
			}
		}
		return retVal;
	}
	/**
	* int get_user_event_index
	* @param mb_hp
	* @param biz_code
	* @return int
	*/
	private static int get_user_event_index(String mb_hp,String biz_code){
		int retVal = 0;
		StringBuilder sb = new StringBuilder();

		MyDataObject dao = new MyDataObject();
		sb.append("SELECT count(*) cnt FROM `cashq`.`user_event_dt` ");
		sb.append("WHERE biz_code=? ");
		sb.append("and mb_hp=? ");
		sb.append("order by seq desc limit 1");
		
		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, biz_code);
			dao.pstmt().setString(2, mb_hp);
			
			dao.setRs (dao.pstmt().executeQuery());

			if (dao.rs().next()) {
				retVal = dao.rs().getInt("cnt");
			}			
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS039";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS040";
		}
		finally {
			dao.closePstmt();
		}

		return retVal;
	}


	/**
	 * set_user_event_dt에 추가한다.
	 * @param String biz_code	콜로그 상태 코드
	 * @param String mb_hp	콜로그 시작시간
	 * @param String conn_edt	콜로그 종료시간
	 * @param String service_sdt	콜로그 제공시간
	 * @param String safen	안심번호
	 * @param String safen_in	링크된번호
	 * @param String safen_out	소비자 번호
	 * @param String calllog_rec_file	
	 * @return
	 */
	public static int set_user_event_dt(String biz_code, 
		String mb_hp, 
		String daily_st_dt,
		String daily_ed_dt,
		String eventcode) 
	{

		boolean retVal = false;
		int last_id = 0;
		StringBuilder sb = new StringBuilder();
		MyDataObject dao = new MyDataObject();
		sb.append("INSERT INTO `cashq`.`user_event_dt` SET ");
		sb.append("biz_code=?,");
		sb.append("mb_hp=?,");
		sb.append("ev_st_dt=?,");
		sb.append("ev_ed_dt=?,");
		sb.append("eventcode=?,");
		sb.append("insdate=now()");

		/*
		sb.append("insert into cashq.site_push_log set "
				+ "stype='SMS', biz_code='ANP', caller=?, called=?, wr_subject=?, regdate=now(), result=''");
		*/
		try {
			dao.openPstmt(sb.toString());

			dao.pstmt().setString(1, biz_code);
			dao.pstmt().setString(2, mb_hp);
			dao.pstmt().setString(3, daily_st_dt);
			dao.pstmt().setString(4, daily_ed_dt);
			dao.pstmt().setString(5, eventcode);

			//dao.pstmt().executeQuery();
			dao.pstmt().executeUpdate();
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS060";
			/* grant로 해당 사용자에 대한 권한을 주어 문제 해결이 가능하다.
			grant all privileges on cashq.site_push_log to sktl@"%" identified by 'sktl@9495';
			grant all privileges on cashq.site_push_log to sktl@"localhost" identified by 'sktl@9495';
			 */
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS061";
		} finally {
			dao.closePstmt();
		}
		return last_id;
	}
	/**
	* get_userevent(biz_code, mb_hp)
	* @param biz_code
	* @param mb_hp
	* @return array
	*/
	private static String[] get_userevent(String biz_code, String mb_hp) {
		String[] s = new String[3];
		StringBuilder sb = new StringBuilder();

		MyDataObject dao = new MyDataObject();
		sb.append("SELECT  ");
		sb.append("eventcode,");
		sb.append("ev_ed_dt,");
		sb.append("ev_st_dt ");
		sb.append("FROM `cashq`.`user_event_dt` ");
		sb.append("WHERE biz_code=? ");
		sb.append("AND mb_hp=? ");
		sb.append("ORDER BY seq desc limit 1;");

		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, biz_code);
			dao.pstmt().setString(2, mb_hp);
			
			dao.setRs (dao.pstmt().executeQuery());

			if (dao.rs().next()) {
				s[0] = dao.rs().getString("ev_st_dt");
				s[1] = dao.rs().getString("ev_ed_dt");
				s[2] = dao.rs().getString("eventcode");
			}			
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS039";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS040";
		}
		finally {
			dao.closePstmt();
		}

		return s;
	}

	
	/**
	 * app_toeken 아이디의 지역 정보를 갱신해서 넣는다.

	 * @param biz_code
	 * @param mb_hp
	 * @return void
	 */
	private static void set_app_token_id(String biz_code,String mb_hp) {

		MyDataObject dao = new MyDataObject();
		
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("UPDATE `cashq`.`app_token_id` SET biz_code=? where tel=?");

			dao.openPstmt(sb.toString());

			dao.pstmt().setString(1, biz_code);
			dao.pstmt().setString(2, mb_hp);

			int cnt = dao.pstmt().executeUpdate();
			if(cnt!=1) {
				Utils.getLogger().warning(dao.getWarning(cnt,1));
				DBConn.latest_warning = "ErrPOS034";
			}

			dao.tryClose();


		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS037";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS038";
			Utils.getLogger().warning(Utils.stack(e));
		}
		finally {
			dao.closePstmt();
		}
	}


	/**
	 * set_stgcm 아이디의 지역 정보를 갱신해서 넣는다.
	 * set_stgcm(safen, safen_in);
	 * @param safen
	 * @param safen_in
	 * @return void
	 */
	private static void set_stgcm(String safen,String safen_in) 
	{

		MyDataObject dao = new MyDataObject();
		
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO cashq.st_gcm SET VIRTUAL_NUM=?,CALLED_NUM=?,insdate=now();");

			dao.openPstmt(sb.toString());

			dao.pstmt().setString(1, safen);
			dao.pstmt().setString(2, safen_in);

			int cnt = dao.pstmt().executeUpdate();
			if(cnt!=1) {
				Utils.getLogger().warning(dao.getWarning(cnt,1));
				DBConn.latest_warning = "ErrPOS034";
			}

			dao.tryClose();


		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS037";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS038";
			Utils.getLogger().warning(Utils.stack(e));
		}
		finally {
			dao.closePstmt();
		}
	}


	/**
	* boolean is_point
	* @param pre_pay
	* @return boolean
	*/
	private static boolean is_point(String pre_pay){
		boolean retVal=false;
		
		if(pre_pay!=null){
			retVal = pre_pay.equals("gl")||pre_pay.equals("sl")||pre_pay.equals("on")||pre_pay.equals("br");
		}
		
		return retVal;
	}

	/**
	* boolean is_datepoint
	* @param ev_st_dt
	* @param ev_ed_dt
	* @return boolean
	*/
	private static boolean is_datepoint(String ev_st_dt,String ev_ed_dt){
		boolean is_date=false;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try{

		/* null check 하나라도 널이면 에러 */
		if(ev_st_dt==null||ev_ed_dt==null){

		}else{
			Date todayDate = new Date();
			
			Date historyDate = sdf.parse(ev_st_dt);
			Date futureDate = sdf.parse(ev_ed_dt);

			/* 기간 이내 */
			is_date=todayDate.after(historyDate)&&todayDate.before(futureDate);
			
			/* 이벤트 종료 시간과 같은 날 */
			if(sdf.format(todayDate).equals(sdf.format(futureDate))){
				is_date=true;
			}		
		}
		
		}catch(ParseException e){
		
		}
		
		return is_date;
	}


	// yyyy-MM-dd HH:mm:ss.0 을 yyyy-MM-dd HH:mm:ss날짜로 변경
	public static String chgDatetime(String str)
	{
		String retVal="";

		try{
		String source = str; 
		SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date historyDate = simpleDate.parse(str);
		retVal=simpleDate.format(historyDate);
		}catch(ParseException e){
		}
		return retVal;
	}


    /**
     * chkValue
	 *  데이터 유효성 null 체크에 대한 값을 "" 로 리턴한다.
     * @param str
     * @return String
     */
	public static String chkValue(String str)
	{
		String retVal="";

		try{
				retVal=str==null?"":str;
		}catch(NullPointerException e){
			
		}
		return retVal;
	}
	
	public static String chk_pt5(String str)
	{
		String retVal="pt5";
		String[] ed_type= new String[] {"freept","freedailypt","freeuserpt"};

		if(Arrays.asList(ed_type).contains(str)){
			retVal="free";
		}

			return retVal; 
	}

	private static String chg_userevent(String eventcode) {
		String retVal="";
		String[] explode=eventcode.split("\\_");
		int up_usercnt=Integer.parseInt(explode[1]);
		up_usercnt++;

		retVal=explode[0]+"_"+up_usercnt;
		return retVal;
	}
	
	/**
	 * 
	 * @param dateString
	 * @return
	 */
	private static long from_unixtime(String dateString)
	{	
		//String dateString = "2017-01-25 20:56:00";
	
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date date;
		long unixTime =0L;
		try {
			date = dateFormat.parse(dateString);
			unixTime = (long) date.getTime()/1000;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return unixTime;
	}



	/**
	 * set_checkpoint
	 * @param cp_type
	 * @param cp_hp
	 * @return
	 */
	public static int set_checkpoint(String cp_type,String cp_hp) 
	{
		boolean retVal = false;
		int last_id = 0;
		long unixtime=0L;
		unixtime=System.currentTimeMillis() / 1000;
		StringBuilder sb = new StringBuilder();
		MyDataObject dao = new MyDataObject();
		sb.append("INSERT INTO `cashq`.`check_point` SET ");
		sb.append("cp_date=?,");
		sb.append("cp_unixtime=?,");
		sb.append("cp_type=?,");
		sb.append("cp_hp=?");
		try {
			dao.openPstmt(sb.toString());

			//Utils.getLogger().warning(sb.toString());

			
			dao.pstmt().setString(1, Utils.getyyyymmdd());
			dao.pstmt().setLong(2, unixtime);
			dao.pstmt().setString(3, cp_type);
			dao.pstmt().setString(4, cp_hp);
			dao.pstmt().executeUpdate();


			
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS060";
			 
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS061";
		} finally {
			dao.closePstmt();
		}

		return last_id;
	}

	
	/**
	* int get_eventcnt
	* @param mb_hp
	* @param eventcode
	* @return int
	*/
	private static int get_checkpoint(String cp_type, String cp_hp){
		int retVal = 0;
		StringBuilder sb = new StringBuilder();

		MyDataObject dao = new MyDataObject();
		sb.append("SELECT count(*) cnt FROM `cashq`.`check_point` ");
		sb.append("WHERE cp_hp=? ");
		sb.append("AND cp_type=? ");
		sb.append("AND cp_date=? ");
		

		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1,	cp_hp);
			dao.pstmt().setString(2, cp_type);
			dao.pstmt().setString(3, Utils.getyyyymmdd());
			
			dao.setRs (dao.pstmt().executeQuery());

			if (dao.rs().next()) {
				retVal = dao.rs().getInt("cnt");
			}			
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS039";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS040";
		}
		finally {
			dao.closePstmt();
		}

		return retVal;
	}



	/**
     * ResultSet을 Row마다 Map에 저장후 List에 다시 저장.
     * @param rs DB에서 가져온 ResultSet
     * @return Listt<map> 형태로 리턴
     * @throws Exception Collection
     */
    private static Map<String, String> getResultMapRows(ResultSet rs) throws Exception
    {
        // ResultSet 의 MetaData를 가져온다.
        ResultSet metaData = (ResultSet) rs;
        // ResultSet 의 Column의 갯수를 가져온다.
        
        int sizeOfColumn = metaData.getMetaData().getColumnCount();
        
        Map<String, String> list = new HashMap<String, String>();
        
        String column_name;
        
        // rs의 내용을 돌려준다.
        if(sizeOfColumn>0)
        {
            // Column의 갯수만큼 회전
            for (int indexOfcolumn = 0; indexOfcolumn < sizeOfColumn; indexOfcolumn++)
            {
                column_name = metaData.getMetaData().getColumnName(indexOfcolumn + 1);
                // map에 값을 입력 map.put(columnName, columnName으로 getString)
                list.put(column_name,rs.getString(column_name));

            }
        }
        return list;
    }
    // 출처: https://moonleafs.tistory.com/52 [달빛에 스러지는 낙엽들.]
    
	
	/**
	 *  add_second 함수를 실행한다.
	 * @param string_date "2019-06-26 09:18:40" 형식으로 입력
	 * @param add_second 더할 시간 만큼 초를 Int형으로 입력한다.
	 * @return String 으로 리턴한다.
	 */
	private static String add_second(String string_date, int add_second)
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
   		Date date = null;

  		try {
  			date = dateFormat.parse(string_date);
  		} catch (ParseException e) {
  			// TODO Auto-generated catch block
           e.printStackTrace();
           System.err.println("형식이 올바르지 않습니다.");
        }

		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.SECOND, add_second);
		//System.out.println(date);
		// System.out.println(cal.getTime());
		String strDate = dateFormat.format(cal.getTime());
		return strDate;
	}    

	
	/**
	 * 처리된 시아이디 포인트 로그 생성한다.
	 * @param String safen_out
	 * @param String calllog_rec_file
	 * @return
	 */
	public static int done_cidpoint_log(String hist_table,String cp_no) 
	{
		boolean retVal = false;
		int last_id = 0;
		StringBuilder sb = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		
		MyDataObject dao = new MyDataObject();
		MyDataObject dao2 = new MyDataObject();
		MyDataObject dao3 = new MyDataObject();
		
		sb.append("insert into prq.");
		sb.append(hist_table);
		sb.append(" select * from prq.prq_cidpoint_log where cp_no=?");

		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, cp_no);
	
			int resultCnt2 = dao.pstmt().executeUpdate();
			if(resultCnt2!=1) {
				Utils.getLogger().warning(dao.getWarning(resultCnt2,1));
				DBConn.latest_warning = "ErrPOS027";
			}
			sb2.append("delete from prq.prq_cidpoint_log where cp_no=?");
	
			dao2.openPstmt(sb2.toString());
			dao2.pstmt().setString(1, cp_no);
	
			int resultCnt3 = dao2.pstmt().executeUpdate();
			if(resultCnt3!=1) {
				Utils.getLogger().warning(dao3.getWarning(resultCnt3,1));
				DBConn.latest_warning = "ErrPOS028";
			}

			
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS060";
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS061";
		} finally {
			dao.closePstmt();
			dao2.closePstmt();
		}	

		return last_id;
	}

}


